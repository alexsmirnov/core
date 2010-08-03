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

package org.ajax4jsf.cache.lru;

import org.ajax4jsf.cache.Cache;
import org.ajax4jsf.cache.CacheFactory;
import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author Nick - mailto:nbelaevski@exadel.com
 *         created 01.05.2007
 */
public class LRUMapCacheFactory implements CacheFactory {
    public static final String CACHE_SIZE_PARAMETER = "org.ajax4jsf.cache.LRU_MAP_CACHE_SIZE";
    private static final Logger LOG = RichfacesLogger.CACHE.getLogger();

    public Cache createCache(Map<?, ?> env) {
        LOG.info("Creating LRUMap cache instance using parameters: " + env);

        String size = (String) env.get(CACHE_SIZE_PARAMETER);

        if ((size == null) || (size.length() == 0)) {
            LOG.info("Creating LRUMap cache instance of default capacity");

            return new LRUMapCache();
        } else {
            int parsedSize = Integer.parseInt(size);

            LOG.info("Creating LRUMap cache instance of " + parsedSize + " items capacity");

            return new LRUMapCache(parsedSize);
        }
    }
}
