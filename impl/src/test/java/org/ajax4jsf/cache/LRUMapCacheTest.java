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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ajax4jsf.cache.lru.CacheEntry;
import org.ajax4jsf.cache.lru.CacheMap;
import org.ajax4jsf.cache.lru.LRUMapCache;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class LRUMapCacheTest {
    @Test
    public void testBasic() throws Exception {
        LRUMapCache cache = new LRUMapCache();

        cache.start();
        Assert.assertNull(cache.get("key"));
        cache.put("key", "value", null);
        Assert.assertEquals("value", cache.get("key"));
        cache.stop();
    }

    @Test
    public void testLRUEviction() throws Exception {
        LRUMapCache cache = new LRUMapCache(3);

        cache.start();
        cache.put("key1", "value1", null);
        cache.put("key2", "value2", null);
        cache.put("key3", "value3", null);
        Assert.assertEquals("value1", cache.get("key1"));
        Assert.assertEquals("value2", cache.get("key2"));
        Assert.assertEquals("value3", cache.get("key3"));
        cache.get("key1");
        cache.get("key3");
        cache.put("key4", "value4", null);
        Assert.assertEquals("value1", cache.get("key1"));
        Assert.assertNull(cache.get("key2"));
        Assert.assertEquals("value3", cache.get("key3"));
        Assert.assertEquals("value4", cache.get("key4"));
        cache.stop();
    }

    @Test
    public void testCacheMap() throws Exception {
        CacheMap cacheMap = new CacheMap();

        Assert.assertTrue(cacheMap.getExpirationQueue().isEmpty());

        CacheEntry cacheEntry = new CacheEntry("key", "value", new Date(System.currentTimeMillis() + 1000));

        cacheMap.put("key", cacheEntry);
        Assert.assertNotNull(cacheMap.get("key"));
        Assert.assertSame(cacheEntry, cacheMap.get("key"));
        Assert.assertFalse(cacheMap.getExpirationQueue().isEmpty());
        cacheMap.clear();
        Assert.assertTrue(cacheMap.getExpirationQueue().isEmpty());
        cacheMap.put("key2", new CacheEntry("key2", "value2", new Date(System.currentTimeMillis() + 1000)));
        Assert.assertNotNull(cacheMap.get("key2"));
        cacheMap.remove("key2");
        Assert.assertTrue(cacheMap.getExpirationQueue().isEmpty());
    }

    @Test
    public void testExpiration() throws Exception {

        // this test uses Thread.sleep, so may fail if debugged
        LRUMapCache cache = new LRUMapCache();

        cache.start();

        long baseTime = System.currentTimeMillis();

        cache.put("key", "value", new Date(baseTime + 2000));
        cache.put("key2", "value2", new Date(baseTime + 3000));
        cache.put("key3", "value3", new Date(baseTime + 1000));
        cache.put("key4", "value4", new Date(baseTime + 1000));
        Assert.assertEquals("value", cache.get("key"));
        Assert.assertEquals("value2", cache.get("key2"));
        Assert.assertEquals("value3", cache.get("key3"));
        Assert.assertEquals("value4", cache.get("key4"));

        // prolong key4
        cache.put("key4", "new value", new Date(baseTime + 2000));
        Thread.sleep(1500);
        Assert.assertEquals("value", cache.get("key"));
        Assert.assertEquals("value2", cache.get("key2"));
        Assert.assertNull(cache.get("key3"));
        Assert.assertEquals("new value", cache.get("key4"));
        Thread.sleep(1000);
        Assert.assertNull(cache.get("key"));
        Assert.assertEquals("value2", cache.get("key2"));
        Assert.assertNull(cache.get("key3"));
        Assert.assertNull(cache.get("key4"));
        Thread.sleep(1000);
        Assert.assertNull(cache.get("key"));
        Assert.assertNull(cache.get("key2"));
        Assert.assertNull(cache.get("key3"));
        cache.stop();
    }

    @Test
    public void testThreads() throws Exception {
        final AtomicBoolean failure = new AtomicBoolean();
        final LRUMapCache cache = new LRUMapCache();

        cache.start();

        Thread[] writerThreads = new Thread[10];

        for (int i = 0; i < writerThreads.length; i++) {
            writerThreads[i] = new Thread() {
                public void run() {
                    final String key = UUID.randomUUID().toString();
                    final String value = UUID.randomUUID().toString();

                    cache.put(key, value, null);

                    Thread[] threads = new Thread[25];

                    for (int j = 0; j < threads.length; j++) {
                        threads[j] = new Thread() {
                            @Override
                            public void run() {
                                int retries = 1000;

                                for (int k = 0; k < retries; k++) {
                                    if (!value.equals(cache.get(key))) {
                                        failure.set(true);

                                        return;
                                    }
                                }
                            }
                        };
                    }

                    for (Thread thread : threads) {
                        thread.start();
                    }

                    int retries = 1000;

                    for (int k = 0; k < retries; k++) {
                        if (!value.equals(cache.get(key))) {
                            failure.set(true);
                        }
                    }

                    for (Thread thread : threads) {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {

                            // TODO Auto-generated catch block
                            failure.set(true);
                        }
                    }
                }
                ;
            };
        }

        for (Thread thread : writerThreads) {
            thread.start();
        }

        for (Thread thread : writerThreads) {
            thread.join();
        }

        Assert.assertFalse(failure.get());
        cache.stop();
    }
}
