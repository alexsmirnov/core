/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.application;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.jboss.test.faces.mock.MockFacesEnvironment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nick Belaevski
 * 
 */
public class ServiceTrackerTest {

    private MockFacesEnvironment environment;
    
    private FacesContext context;
    
    private ServletContext servletContext;
    
    private Map<String, Object> applicationMap;
    
    private Map<Object, Object> contextMap;

    private SkinServiceImpl skinServiceImpl;

    private ConfigServiceImpl configServiceImpl;

    private ConfigServiceReferenceImpl configServiceReference;
    
    private static interface SkinService {};
    
    private static class SkinServiceImpl implements SkinService {}
    
    private static interface ConfigService {};

    private static class ConfigServiceReferenceImpl implements ServiceReference<ConfigService> {

        private ConfigService service;
        
        public ConfigServiceReferenceImpl(ConfigService service) {
            super();
            this.service = service;
        }

        public ConfigService getService() {
            return service;
        }
    }

    private static class ConfigServiceImpl implements ConfigService {}

    private void setupRequestObjects() {
        expect(environment.getExternalContext().getApplicationMap()).andStubReturn(applicationMap);
        expect(environment.getExternalContext().getContext()).andStubReturn(servletContext);
        
        contextMap = new HashMap<Object, Object>();
        expect(environment.getFacesContext().getAttributes()).andStubReturn(contextMap);
    }
    
    @Before
    public void setUp() throws Exception {
        skinServiceImpl = new SkinServiceImpl();
        configServiceImpl = new ConfigServiceImpl();
        configServiceReference = new ConfigServiceReferenceImpl(configServiceImpl);
        
        environment = MockFacesEnvironment.createEnvironment().withExternalContext();
        context = environment.getFacesContext();

        applicationMap = new HashMap<String, Object>();
        servletContext = environment.createMock(ServletContext.class);
        
        setupRequestObjects();
    }
    
    @After
    public void tearDown() throws Exception {
        skinServiceImpl = null;
        configServiceImpl = null;
        configServiceReference = null;

        context = null;
        applicationMap = null;
        contextMap = null;
        
        environment.verify();
        environment.release();
        environment = null;
    }
    
    @Test
    public void testBasic() throws Exception {
        environment.replay();
        
        ServiceTracker.setService(context, SkinService.class, skinServiceImpl);
        ServiceTracker.setServiceReference(context, ConfigService.class, configServiceReference);
        
        ServiceTracker.lockModification(context);
        
        Collection<Class<?>> serviceClasses = ServiceTracker.getRegisteredServiceClasses(context);
        assertFalse(serviceClasses.isEmpty());
        
        assertTrue(serviceClasses.contains(SkinService.class));
        assertTrue(serviceClasses.contains(ConfigService.class));
        
        assertSame(skinServiceImpl, ServiceTracker.getService(context, SkinService.class));
        assertSame(configServiceImpl, ServiceTracker.getService(context, ConfigService.class));
        
        environment.reset();
        setupRequestObjects();
        environment.replay();

        serviceClasses = ServiceTracker.getRegisteredServiceClasses(context);
        assertFalse(serviceClasses.isEmpty());
        
        assertTrue(serviceClasses.contains(SkinService.class));
        assertTrue(serviceClasses.contains(ConfigService.class));
        
        assertSame(skinServiceImpl, ServiceTracker.getService(context, SkinService.class));
        assertSame(configServiceImpl, ServiceTracker.getService(context, ConfigService.class));
        
        ServiceTracker.release(context);
    }

    @Test
    public void testLockModifications() throws Exception {
        environment.replay();

        ServiceTracker.lockModification(context);
        
        try {
            ServiceTracker.setService(context, SkinService.class, skinServiceImpl);
            
            fail();
        } catch (IllegalStateException e) {
        }

        ServiceTracker.release(context);
    }
}