/**
 *
 */
package org.ajax4jsf.cache;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.ajax4jsf.context.ContextInitParameters;
import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class EhCacheCacheFactory implements CacheFactory {
    
    private static final Logger LOG = RichfacesLogger.CACHE.getLogger();

    private CacheManager cacheManager;
    
    public EhCacheCacheFactory() {
        super();
        
        URL configUrl = null;
        
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl != null) {
            configUrl = ccl.getResource("ehcache.xml");
        }

        if (configUrl != null) {
            LOG.info(MessageFormat.format("Using cache configuration: {0}", configUrl.toExternalForm()));
        } else {
            configUrl = EhCacheCacheFactory.class.getResource("ehcache-failsafe-richfaces.xml");
            LOG.info(MessageFormat.format("Using default cache configuration: {0}", configUrl.toExternalForm()));
        }

        cacheManager = CacheManager.create(configUrl);
    }
    
    public Cache createCache(FacesContext facesContext, String cacheName, Map<?, ?> env) {
        LOG.info("Creating EhCache cache instance");

        int maxCacheSize = ContextInitParameters.getResourcesCacheSize(facesContext);
        boolean preconfiguredCache = false;
        
        Ehcache ehcache = cacheManager.getEhcache(cacheName);
        if (ehcache == null) {
            ehcache = new net.sf.ehcache.Cache(cacheName, maxCacheSize, false, true, 0, 0);
        } else {
            preconfiguredCache = true;
            
            if (ehcache.getCacheConfiguration().getMaxElementsInMemory() <= 0) {
                LOG.info(MessageFormat.format("Maximum cache size hasn''t been set, resetting to {0} max items", maxCacheSize));
                
                ehcache.getCacheConfiguration().setMaxElementsInMemory(maxCacheSize);
            }
        }
        
        return new EhCacheCache(ehcache, preconfiguredCache);
    }
    
    public void destroy() {
        cacheManager.shutdown();
        cacheManager = null;
    }
        
}
