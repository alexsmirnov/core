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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.faces.context.FacesContext;

/**
 * <p>Tracker class to provide access to various framework implementation services.
 * Examples of such services are: {@link org.richfaces.skin.SkinFactory}, TBD.</p>
 * 
 * <p>Supports either direct placement of service implementation instances or lazy 
 * initialization via {@link ServiceReference} interface.</p>
 * 
 * <p>This class represents application-scoped object that is replicated into attributes 
 * of {@link FacesContext} during runtime for better performance.</p>
 * 
 * <p>No modifications operations are allowed after {@link ServiceTracker} is locked, 
 * and {@link IllegalStateException} is throws in this case.</p>
 * 
 * <p><b>Note:</b> in initial state this class is not synchronized and presumes that all 
 * modification operations are done in a context of single-thread (in JSF initialization listener). 
 * In locked state read operations can be called from multiple threads concurrently without no need 
 * to synchronize explicitly.</p>
 *
 * @author Nick Belaevski
 * @since 4.0
 */
public final class ServiceTracker {

    private static final String SERVICE_TRACKER_ATTRIBUTE = ServiceTracker.class.getName();

    private volatile Map<Class<?>, Object> servicesMap = new HashMap<Class<?>, Object>();
    
    private final Date startTime = new Date();
    
    private final ConcurrentMap<Object, Object> concurrentStorage = new ConcurrentHashMap<Object, Object>();
    
    private ServiceTracker() {
        //utility class private constructor
    }

    private <T> T get(Class<T> serviceClass) {
        Object serviceImplementation = null;
        Object serviceObject = servicesMap.get(serviceClass);
        
        if (serviceObject instanceof ServiceReference<?>) {
            serviceImplementation = ((ServiceReference<?>) serviceObject).getService();
        } else {
            serviceImplementation = serviceObject;
        }

        //TODO - null?
        return serviceClass.cast(serviceImplementation);
    } 
    
    private void put(Class<?> key, Object value) {
        try {
            servicesMap.put(key, value);
        } catch (UnsupportedOperationException e) {
            throw new IllegalStateException("Service tracker is locked, no modification operation is allowed!");
        }
    }

    private Collection<Class<?>> getRegisteredServiceClasses() {
        return Collections.unmodifiableCollection(servicesMap.keySet());
    }
    
    private synchronized void lockModification() {
        servicesMap = Collections.unmodifiableMap(servicesMap);
    }
    
    /**
     * Returns unmodifiable collection of registered service classes.
     * 
     * @param context current instance of {@link FacesContext}
     * @return collection of registered service classes
     * 
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    public static Collection<Class<?>> getRegisteredServiceClasses(FacesContext context) {
        if (context == null) {
            throw new NullPointerException("context");
        }

        ServiceTracker serviceTracker = getServiceTracker(context);
        return serviceTracker.getRegisteredServiceClasses();
    }

    /**
     * Lookup registered service implementation by service class.
     * 
     * @param context current instance of {@link FacesContext}
     * @param serviceClass class for which implementation has been registered.
     * @return registered implementation or <code>null</code>
     * 
     * @throws NullPointerException if <code>context</code> or <code>serviceClass</code> is <code>null</code>
     */
    public static <T> T getService(FacesContext context, Class<T> serviceClass) {
        if (context == null) {
            throw new NullPointerException("context");
        }

        if (serviceClass == null) {
            throw new NullPointerException("serviceClass");
        }

        ServiceTracker serviceTracker = getServiceTracker(context);
        return serviceTracker.get(serviceClass);
    }

    /**
     * Registers service implementation for the given service class.
     * 
     * @param context current instance of {@link FacesContext}
     * @param serviceClass class for which implementation is to be registered
     * @param serviceImplementation service implementation
     * 
     * @throws NullPointerException if <code>context</code>, <code>serviceClass</code> or 
     * <code>serviceImplementation</code> is <code>null</code>
     * @throws IllegalStateException if current {@link ServiceTracker} is in locked state.
     */
    public static <T> void setService(FacesContext context,
        Class<T> serviceClass,
        T serviceImplementation) {

        if (context == null) {
            throw new NullPointerException("context");
        }

        if (serviceClass == null) {
            throw new NullPointerException("serviceClass");
        }

        if (serviceImplementation == null) {
            throw new NullPointerException("serviceImplementation");
        }

        ServiceTracker serviceTracker = getServiceTracker(context);
        serviceTracker.put(serviceClass, serviceImplementation);
    }

    /**
     * Registers {@link ServiceReference} for the given service class. 
     * 
     * @param context current instance of {@link FacesContext}
     * @param serviceClass class for which reference is to be registered
     * @param serviceReference instance of service reference
     * 
     * @throws NullPointerException if <code>context</code>, <code>serviceClass</code> or 
     * <code>serviceReference</code> is <code>null</code>
     * @throws IllegalStateException if current {@link ServiceTracker} is in locked state.
     */
    public static <T> void setServiceReference(FacesContext context,
        Class<T> serviceClass,
        ServiceReference<T> serviceReference) {

        if (context == null) {
            throw new NullPointerException("context");
        }

        if (serviceClass == null) {
            throw new NullPointerException("serviceClass");
        }

        if (serviceReference == null) {
            throw new NullPointerException("serviceReference");
        }

        ServiceTracker serviceTracker = getServiceTracker(context);
        serviceTracker.put(serviceClass, serviceReference);
    }

    /**
     * <p>Releases application-scoped {@link ServiceTracker}.</p>
     * <p>Called during application shutdown; shouldn't be called explicitly by user.</p>
     * 
     * @param context current instance of {@link FacesContext}
     */
    public static void release(FacesContext context) {
        removeServiceTracker(context);
    }

    /**
     * <p>Switches application-scoped {@link ServiceTracker} to locked state, preventing further modifications 
     * of registered services via {@link #setService(FacesContext, Class, Object)} or 
     * {@link #setServiceReference(FacesContext, Class, ServiceReference)} methods.</p>
     * 
     * <p>Called at the beginning of the very first application request life cycle; shouldn't be called explicitly 
     * by user.</p>
     * 
     * @param context current instance of {@link FacesContext}
     */
    public static void lockModification(FacesContext context) {
        ServiceTracker serviceTracker = getServiceTracker(context);
        serviceTracker.lockModification();
    }
    
    /**
     * Returns {@link ServiceTracker} instantiation time. Corresponds to application initialization time.
     * 
     * @param context
     * @return instantiation time
     */
    public static Date getStartTime(FacesContext context) {
        return getServiceTracker(context).startTime;
    }
    
    /**
     * Returns {@link ConcurrentMap} stored in {@link ServiceTracker}. This map is intended 
     * to be used as fast application-scoped storage.
     * 
     * @param context
     * @return
     */
    public static ConcurrentMap<Object, Object> getConcurrentStorage(FacesContext context) {
        return getServiceTracker(context).concurrentStorage;
    }
    
    private static ServiceTracker getServiceTrackerFromApplicationMap(FacesContext facesContext) {
        Object appContext = facesContext.getExternalContext().getContext();
        
        synchronized (appContext) {
            ServiceTracker serviceTracker;
            Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();
            serviceTracker = (ServiceTracker) applicationMap.get(SERVICE_TRACKER_ATTRIBUTE);
            if (serviceTracker == null) {
                serviceTracker = new ServiceTracker();
                applicationMap.put(SERVICE_TRACKER_ATTRIBUTE, serviceTracker);
            }
            
            return serviceTracker;
        }
        
    }
    
    private static ServiceTracker getServiceTracker(FacesContext facesContext) {
        ServiceTracker serviceTracker = (ServiceTracker) facesContext.getAttributes().get(SERVICE_TRACKER_ATTRIBUTE);

        if (serviceTracker == null) {
            serviceTracker = getServiceTrackerFromApplicationMap(facesContext);
            
            //replicate in FacesContext map for better performance
            facesContext.getAttributes().put(SERVICE_TRACKER_ATTRIBUTE, serviceTracker);
        }

        return serviceTracker;
    }

    private static void removeServiceTracker(FacesContext facesContext) {
        Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();
        applicationMap.remove(SERVICE_TRACKER_ATTRIBUTE);

        facesContext.getAttributes().remove(SERVICE_TRACKER_ATTRIBUTE);
    }
}
