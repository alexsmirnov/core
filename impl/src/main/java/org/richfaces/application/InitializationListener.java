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

import java.text.MessageFormat;
import java.util.ArrayList;

import javax.faces.FacesException;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import org.richfaces.VersionBean;
import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class InitializationListener implements SystemEventListener {

    private static final Logger LOGGER = RichfacesLogger.APPLICATION.getLogger();
    
    /* (non-Javadoc)
      * @see javax.faces.event.SystemEventListener#isListenerForSource(java.lang.Object)
      */
    public boolean isListenerForSource(Object source) {
        return true;
    }

    protected void onStart() {
        ServicesFactory injector = createFactory(); 
        ServiceTracker.setFactory(injector);

        if (LOGGER.isInfoEnabled()) {
            String versionString = VersionBean.VERSION.toString();
            if (versionString != null && versionString.length() != 0) {
                LOGGER.info(versionString);
            }
        }
    }

    protected ServicesFactory createFactory() {
        ServicesFactoryImpl injector = new ServicesFactoryImpl();
        ArrayList<Module> modules = new ArrayList<Module>();
        modules.add(new DefaultModule());
        try {
        modules.addAll(ServiceLoader.loadServices(Module.class));
        injector.init(modules);
        } catch (ServiceException e) {
            throw new FacesException(e);
        }
        return injector;
    }

    protected void onStop() {
        ServiceTracker.release();
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
