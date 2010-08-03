/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and individual contributors
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import org.ajax4jsf.cache.CacheManager;
import org.ajax4jsf.renderkit.AJAXDataSerializer;
import org.ajax4jsf.resource.util.URLToStreamHelper;
import org.richfaces.log.RichfacesLogger;
import org.richfaces.skin.SkinFactory;
import org.richfaces.skin.SkinFactoryImpl;
import org.slf4j.Logger;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class InitializationListener implements SystemEventListener {

    private static final String META_INF_SERVICES = "META-INF/services/";

    private static final Logger LOG = RichfacesLogger.APPLICATION.getLogger();

    /* (non-Javadoc)
      * @see javax.faces.event.SystemEventListener#isListenerForSource(java.lang.Object)
      */
    public boolean isListenerForSource(Object source) {
        return true;
    }

    private static <T> T instantiate(Class<T> interfaceClass,
                                     Class<? extends T> implementationClass,
                                     Class<? extends T> defaultImplementationClass) {

        Constructor<? extends T> constructor = null;
        Object[] constructorArguments = null;

        if (implementationClass != null) {
            if (defaultImplementationClass != null && !defaultImplementationClass.equals(implementationClass)) {
                try {
                    constructor = implementationClass.getConstructor(interfaceClass);
                    T defaultImplementation = instantiate(interfaceClass, defaultImplementationClass, null);
                    constructorArguments = new Object[]{defaultImplementation};
                } catch (NoSuchMethodException e) {
                    /* ignore */
                }
            }

            if (constructor == null) {
                try {
                    constructor = implementationClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new FacesException(MessageFormat.format("Class {0} has no public no-arg constructor",
                        implementationClass.getName()), e);
                }
            }

        } else {
            try {
                constructor = defaultImplementationClass.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new FacesException(MessageFormat.format("Class {0} has no public no-arg constructor",
                    defaultImplementationClass.getName()), e);
            }
        }

        try {
            return constructor.newInstance(constructorArguments);
        } catch (IllegalArgumentException e) {
            throw new FacesException(MessageFormat.format("Cannot instantiate {0} class, error was: {1}",
                constructor.getDeclaringClass(), e.getMessage()), e);
        } catch (InstantiationException e) {
            throw new FacesException(MessageFormat.format("Cannot instantiate {0} class, error was: {1}",
                constructor.getDeclaringClass(), e.getMessage()), e);
        } catch (IllegalAccessException e) {
            throw new FacesException(MessageFormat.format("Cannot instantiate {0} class, error was: {1}",
                constructor.getDeclaringClass(), e.getMessage()), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new FacesException(MessageFormat.format("Cannot instantiate {0} class, error was: {1}",
                constructor.getDeclaringClass(), cause.getMessage()), cause);
        }
    }

    private static <T> T createServiceInstance(Class<T> interfaceClass, Class<? extends T> defaultImplementationClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream input = URLToStreamHelper.urlToStreamSafe(
            loader.getResource(META_INF_SERVICES + interfaceClass.getName()));

        Class<? extends T> implementationClass = null;

        // have services file.
        if (input != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String factoryClassName = reader.readLine();

                implementationClass = Class.forName(factoryClassName, false, loader).asSubclass(interfaceClass);
            } catch (Exception e) {
                LOG.warn(MessageFormat.format("Error loading class for {0} service: {1} ",
                    interfaceClass.getName(), e.getMessage()), e);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    // can be ignored
                }
            }
        }

        return instantiate(interfaceClass, implementationClass, defaultImplementationClass);
    }

    protected void onStart() {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        SkinFactory skinFactory = createServiceInstance(SkinFactory.class, SkinFactoryImpl.class);
        ServiceTracker.setService(facesContext, SkinFactory.class, skinFactory);

        AJAXDataSerializer dataSerializer = createServiceInstance(AJAXDataSerializer.class, AJAXDataSerializer.class);
        ServiceTracker.setService(facesContext, AJAXDataSerializer.class, dataSerializer);
    }

    protected void onStop() {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        ServiceTracker.release(facesContext);
        CacheManager.getInstance().destroy();
    }

    /* (non-Javadoc)
      * @see javax.faces.event.SystemEventListener#processEvent(javax.faces.event.SystemEvent)
      */
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        if (event instanceof PostConstructApplicationEvent) {
            onStart();
        } else if (event instanceof PreDestroyApplicationEvent) {
            onStop();
        } else {
            throw new IllegalArgumentException(MessageFormat.format("Event {0} is not supported!", event));
        }
    }

}
