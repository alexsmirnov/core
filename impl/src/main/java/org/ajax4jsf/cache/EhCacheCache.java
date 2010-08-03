/**
 *
 */
package org.ajax4jsf.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.util.Date;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class EhCacheCache implements org.ajax4jsf.cache.Cache {
    private net.sf.ehcache.Ehcache cache;

    public EhCacheCache(Ehcache cache) {
        super();
        this.cache = cache;
    }

    public void start() {
        cache.initialise();
        cache.bootstrap();
    }

    public void stop() {
        cache.dispose();
    }

    public Object get(Object key) {
        Element element = cache.get(key);

        if (element != null) {
            return element.getObjectValue();
        }

        return null;
    }

    public void put(Object key, Object value, Date expired) {
        Boolean eternal = null; // use cache defaults
        Integer ttl = null;

        if (expired != null) {
            eternal = Boolean.FALSE;
            ttl = (int) (expired.getTime() - System.currentTimeMillis()) / 1000;
        }

        Element element = new Element(key, value, eternal, null, ttl);

        cache.putQuiet(element);
    }
}
