/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.ajax4jsf.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;

import org.ajax4jsf.context.ContextInitParameters;
import org.ajax4jsf.resource.util.URLToStreamHelper;
import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

import com.opensymphony.oscache.base.AbstractCacheAdministrator;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * @author Nick - mailto:nbelaevski@exadel.com
 *         created 01.05.2007
 */
public class OSCacheCacheFactory implements CacheFactory {
    
    private static final Logger LOG = RichfacesLogger.CACHE.getLogger();

    private List<GeneralCacheAdministrator> cacheAdministrators = new ArrayList<GeneralCacheAdministrator>(1);
    
    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        URL resource = OSCacheCache.class.getResource("oscache.properties");

        if (resource != null) {
            InputStream stream = URLToStreamHelper.urlToStream(resource);

            try {
                properties.load(stream);
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {

                    // TODO: handle exception
                }
            }
        }

        return properties;
    }

    public OSCacheCacheFactory() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = OSCacheCacheFactory.class.getClassLoader();
        }
        
        //try load cache class to check its presence in classpath
        Class.forName(GeneralCacheAdministrator.class.getName(), false, classLoader);
    }
    
    public Cache createCache(FacesContext facesContext, String cacheName, Map<?, ?> env) {
        //TODO - handle cache name
        Properties cacheProperties = new Properties();

        try {
            cacheProperties.putAll(loadProperties());
        } catch (IOException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            cacheProperties.putAll(loadProperties());
        } catch (IOException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        cacheProperties.putAll(env);
        LOG.info("Creating OSCache cache instance using parameters: " + cacheProperties);

        String property = cacheProperties.getProperty(AbstractCacheAdministrator.CACHE_CAPACITY_KEY);
        if (property == null) {
            int maxCacheSize = ContextInitParameters.getResourcesCacheSize(facesContext);
            LOG.info(MessageFormat.format("Maximum cache size hasn''t been set, resetting to {0} max items", maxCacheSize));
            cacheProperties.put(AbstractCacheAdministrator.CACHE_CAPACITY_KEY, 
                Integer.toString(maxCacheSize));
        }
        
        GeneralCacheAdministrator cacheAdministrator = new GeneralCacheAdministrator(cacheProperties);
        cacheAdministrators.add(cacheAdministrator);
        return new OSCacheCache(cacheAdministrator.getCache());
    }
    
    public void destroy() {
        for (GeneralCacheAdministrator cacheAdministrator : cacheAdministrators) {
            cacheAdministrator.destroy();
        }
        
        cacheAdministrators = null;
    }
}
