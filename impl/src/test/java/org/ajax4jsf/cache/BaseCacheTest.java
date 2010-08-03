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

import java.util.Date;
import java.util.Map;

import org.jboss.test.faces.AbstractFacesTest;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public abstract class BaseCacheTest extends AbstractFacesTest {
    private Cache cache;
    private String cacheManagerFactoryClassName;

    public BaseCacheTest(String cacheManagerFactoryClassName) {
        super();
        this.cacheManagerFactoryClassName = cacheManagerFactoryClassName;
    }

    @Override
    protected void setupJsfInitParameters() {
        super.setupJsfInitParameters();
        this.facesServer.addInitParameter(CacheManager.CACHE_MANAGER_FACTORY_CLASS, cacheManagerFactoryClassName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupFacesRequest();

        CacheManager cacheManager = CacheManager.getInstance();
        Map<?, ?> initParameterMap = facesContext.getExternalContext().getInitParameterMap();

        this.cache = cacheManager.getCacheFactory(initParameterMap).createCache(initParameterMap);
        this.cache.start();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (this.cache != null) {
            this.cache.stop();
            this.cache = null;
        }
    }

    public void testBasic() throws Exception {
        assertNull(cache.get("a"));
        cache.put("a", "value-a", null);
        assertEquals("value-a", cache.get("a"));
    }

    public void testExpiration() throws Exception {
        assertNull(cache.get("a"));

        long sleepTime = 0;
        long expirationTime = System.currentTimeMillis() + 3000;

        cache.put("a", "value-a", new Date(expirationTime));
        assertEquals("value-a", cache.get("a"));

        // interval to reach 1 second before expiration time
        sleepTime = expirationTime - 1000 - System.currentTimeMillis();
        assertTrue(sleepTime > 0);
        Thread.sleep(sleepTime);
        assertEquals("value-a", cache.get("a"));

        // interval to reach 1 second after expiration time
        sleepTime = expirationTime + 1000 - System.currentTimeMillis();
        assertTrue(sleepTime > 0);
        Thread.sleep(sleepTime);
        assertNull(cache.get("a"));
    }
}
