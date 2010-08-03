/**
 *
 */
package org.ajax4jsf.cache;

import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class EhCacheCacheFactory implements CacheFactory {
    private static final Logger LOG = RichfacesLogger.CACHE.getLogger();

    public Cache createCache(Map<?, ?> env) {
        LOG.info("Creating EhCache cache instance");

        net.sf.ehcache.Ehcache cache = new net.sf.ehcache.Cache("org.richfaces", 256, false, true, 0, 0);

        return new EhCacheCache(cache);
    }
}
