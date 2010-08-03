/*
 * Copyright [yyyy] [name of copyright owner].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ajax4jsf.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ajax4jsf.cache.lru.LRUMapCacheFactory;
import org.ajax4jsf.resource.util.URLToStreamHelper;
import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

/**
 * TODO stop caches on application stop
 * CacheManager is used in J2SE environments for looking up named caches.
 */
public class CacheManager {
    public static final String CACHE_MANAGER_FACTORY_CLASS = "org.ajax4jsf.cache.CACHE_MANAGER_FACTORY_CLASS";

    private static final String[] DEFAULT_FACTORIES_CHAIN = {"org.ajax4jsf.cache.JBossCacheCacheFactory",
        "org.ajax4jsf.cache.EhCacheCacheFactory"};
    private static final String FACTORY_PROPERTY_NAME = "org.ajax4jsf.cache.CacheFactory";
    private static final Logger LOG = RichfacesLogger.CACHE.getLogger();
    private static final CacheManager INSTANCE = new CacheManager();

    // REVIEW brian@quiotix.com
    // Should this be a HashMap<String, WeakReference<Cache>>?
    private final Map<String, Cache> caches = Collections.synchronizedMap(new HashMap<String, Cache>());

    /**
     * Returns the singleton CacheManager
     */
    public static CacheManager getInstance() {
        return INSTANCE;
    }

    public Cache getCache(String cacheName) {
        return caches.get(cacheName);
    }

    public Cache getNewCache(String cacheName, Map<?, ?> env) {
        createCache(cacheName, env);

        return getCache(cacheName);
    }

    public void registerCache(String cacheName, Cache cache) {
        caches.put(cacheName, cache);
    }

    public void createCache(String cacheName, Map<?, ?> env) {
        CacheFactory factory = getCacheFactory(env);
        Cache cache = factory.createCache(env);

        registerCache(cacheName, cache);
        cache.start();
    }

    public void destroyCache(String cacheName) {
        Cache cache = caches.remove(cacheName);

        cache.stop();
    }

    public CacheFactory getCacheFactory(Map<?, ?> env) {
        String[] factories;
        String configuredFactoryName = findFactory(FACTORY_PROPERTY_NAME, env);

        if (configuredFactoryName != null) {
            LOG.info(MessageFormat.format("Configured to use [{0}] cache factory", configuredFactoryName));
            factories = new String[]{configuredFactoryName};
        } else {
            factories = DEFAULT_FACTORIES_CHAIN;
        }

        ClassLoader loader = findClassLoader();

        for (String factoryName : factories) {
            try {
                Class<?> spiClass = Class.forName(factoryName, true, loader);
                CacheFactory cacheFactory = CacheFactory.class.cast(spiClass.newInstance());

                LOG.info(MessageFormat.format("Selected [{0}]", factoryName));

                return cacheFactory;
            } catch (Throwable iae) {

                // TODO LOG debug
            }
        }

        LOG.info("Selected fallback cache factory");

        return new LRUMapCacheFactory();
    }

    public Map<String, Cache> getCaches() {
        return caches;
    }

    private ClassLoader findClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        return cl;
    }

    private static boolean isEmptyString(String s) {
        return (s == null) || "".equals(s);
    }

    String findFactory(String factoryId, Map<?, ?> env) {
        String envFactoryClass = (String) env.get(CACHE_MANAGER_FACTORY_CLASS);

        if (!isEmptyString(envFactoryClass)) {
            return envFactoryClass;
        }

        // Use the system property first
        String systemPropertyFactoryClass = searchInSystemProperty(factoryId);

        if (!isEmptyString(systemPropertyFactoryClass)) {
            return systemPropertyFactoryClass;
        }

        // try to read from $java.home/lib/jcache.properties
        String jcacheFactoryClass = searchInJcacheProperties(factoryId);

        if (!isEmptyString(jcacheFactoryClass)) {
            return jcacheFactoryClass;
        }

        // try to find services in CLASSPATH
        String classpathFactoryClass = searchInClasspath(factoryId);

        if (!isEmptyString(classpathFactoryClass)) {
            return classpathFactoryClass;
        }

        return null;
    }

    private String searchInClasspath(String factoryId) {
        try {
            ClassLoader cl = findClassLoader();
            InputStream is = URLToStreamHelper.urlToStreamSafe(cl.getResource("META-INF/services/" + factoryId));

            if (is != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                try {
                    return r.readLine();
                } finally {
                    r.close();
                }
            }
        } catch (IOException ignore) {

            // TODO Refactoring
        }

        return null;
    }

    private static String searchInJcacheProperties(String factoryId) {
        try {
            String configFile = System.getProperty("java.home") + File.separator + "lib" + File.separator
                + "jcache.properties";
            File file = new File(configFile);

            if (file.exists()) {
                InputStream in = new FileInputStream(file);

                try {
                    Properties props = new Properties();

                    props.load(in);

                    return props.getProperty(factoryId);
                } finally {
                    in.close();
                }
            }
        } catch (SecurityException ignore) {

            // TODO Refactoring
        } catch (IOException ignore) {

            // TODO Refactoring
        }

        return null;
    }

    private static String searchInSystemProperty(String factoryId) {
        try {
            return System.getProperty(factoryId);
        } catch (SecurityException ignore) {

            // TODO Refactoring
        }

        return null;
    }

    public void destroy() {
        if (caches.isEmpty()) {
            return;
        }

        for (String cacheName : caches.keySet()) {
            destroyCache(cacheName);
        }
    }
}
