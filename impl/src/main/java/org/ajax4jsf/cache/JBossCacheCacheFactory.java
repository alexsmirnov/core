/**
 *
 */
package org.ajax4jsf.cache;

import org.ajax4jsf.resource.util.URLToStreamHelper;
import org.jboss.cache.Cache;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.EvictionConfig;
import org.jboss.cache.config.EvictionRegionConfig;
import org.jboss.cache.eviction.ExpirationAlgorithmConfig;

import javax.faces.FacesException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * @author Nick Belaevski
 * @since 4.0
 */

//TODO - to doc - no max size eviction support
public class JBossCacheCacheFactory implements CacheFactory {
    private org.jboss.cache.CacheFactory<String, Object> cacheFactory;

    public JBossCacheCacheFactory() {
        super();
        cacheFactory = new DefaultCacheFactory<String, Object>();
    }

    public org.ajax4jsf.cache.Cache createCache(Map<?, ?> env) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Cache<String, Object> cache = null;
        URL cacheConfigurationURL = null;

        if (contextClassLoader != null) {
            cacheConfigurationURL = contextClassLoader.getResource("/jboss-cache.xml");
        }

        if (cacheConfigurationURL != null) {
            InputStream stream = null;

            try {
                stream = URLToStreamHelper.urlToStream(cacheConfigurationURL);
                cache = cacheFactory.createCache(stream);
            } catch (IOException e) {
                throw new FacesException(e.getLocalizedMessage(), e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {

                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Configuration configuration = new Configuration();
            EvictionRegionConfig evictionRegionConfig = new EvictionRegionConfig(Fqn.root());
            ExpirationAlgorithmConfig expirationAlgorithm = new ExpirationAlgorithmConfig();

            evictionRegionConfig.setEvictionAlgorithmConfig(expirationAlgorithm);

            EvictionConfig evictionConfig = new EvictionConfig(evictionRegionConfig);

            evictionConfig.setWakeupInterval(1000);
            configuration.setEvictionConfig(evictionConfig);
            cache = cacheFactory.createCache(configuration);
        }

        return new JBossCacheCache(cache);
    }
}
