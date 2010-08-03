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

/**
 * <p>
 * This interface is coupled with {@link ServiceTracker} class and provides reference to service implementation, 
 * allowing delayed service initialization.
 * </p>
 * 
 * <p>Methods provided by this interface are expected to be called from multiple concurrent threads 
 * without any synchronization aids.</p>
 * 
 * @author Nick Belaevski
 * @since 4.0
 */
public interface ServiceReference<T> {

    /**
     * <p>Returns instance of service referenced by <code>this</code> object.</p>
     * 
     * <p>Calling this method can cause delayed initialization of service. 
     * Clients of this class are not expected to store returned service implementation object, 
     * so storing reference to the created service object to avoid repeated initialization is the 
     * sole responsibility of this class.</p>
     * 
     * @return referenced service implementation object
     */
    public T getService();
    
}
