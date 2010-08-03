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

package org.richfaces.skin;

import org.ajax4jsf.Messages;
import org.richfaces.log.RichfacesLogger;
import org.slf4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyResolver;

/**
 * Resolve Skin propertyes.
 *
 * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:59:41 $
 */
public class SkinPropertyResolver extends PropertyResolver {
    private static final Logger LOG = RichfacesLogger.APPLICATION.getLogger();
    private PropertyResolver parent = null;

    /**
     * @param parent
     */
    public SkinPropertyResolver(PropertyResolver parent) {
        this.parent = parent;
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#getType(java.lang.Object, int)
     */
    @Override
    public Class getType(Object base, int index) {
        if (base instanceof Skin) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.getMessage(Messages.ACESSING_SKIN_PROPERTY_AS_ARRAY_ERROR));
            }

            return null;
        }

        return parent.getType(base, index);
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#getType(java.lang.Object, java.lang.Object)
     */
    @Override
    public Class getType(Object base, Object property) {
        if (base instanceof Skin) {
            Skin skin = (Skin) base;

            if (property instanceof String) {
                return skin.getParameter(FacesContext.getCurrentInstance(), (String) property).getClass();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.getMessage(Messages.ACESSING_SKIN_PROPERTY_ERROR));
            }

            return null;
        }

        return parent.getType(base, property);
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#getValue(java.lang.Object, int)
     */
    @Override
    public Object getValue(Object base, int index) {
        if (base instanceof Skin) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.getMessage(Messages.ACESSING_SKIN_PROPERTY_AS_ARRAY_ERROR));
            }

            return null;
        }

        return parent.getValue(base, index);
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#getValue(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getValue(Object base, Object property) {
        if (base instanceof Skin) {
            Skin skin = (Skin) base;

            if (property instanceof String) {
                return skin.getParameter(FacesContext.getCurrentInstance(), (String) property);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.getMessage(Messages.ACESSING_SKIN_PROPERTY_ERROR));
            }

            return null;
        }

        return parent.getValue(base, property);
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#isReadOnly(java.lang.Object, int)
     */
    @Override
    public boolean isReadOnly(Object base, int arg1) {
        if (base instanceof Skin) {
            return true;
        }

        return parent.isReadOnly(base, arg1);
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#isReadOnly(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isReadOnly(Object base, Object arg1) {
        if (base instanceof Skin) {
            return true;
        }

        return parent.isReadOnly(base, arg1);
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#setValue(java.lang.Object, int, java.lang.Object)
     */
    @Override
    public void setValue(Object base, int index, Object value) {
        if (base instanceof Skin) {
            throw new EvaluationException(Messages.getMessage(Messages.SKIN_PROPERTIES_READ_ONLY_ERROR));
        }

        parent.setValue(base, index, value);
    }

    /*
     *  (non-Javadoc)
     * @see javax.faces.el.PropertyResolver#setValue(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue(Object base, Object property, Object value) {
        if (base instanceof Skin) {
            throw new EvaluationException(Messages.getMessage(Messages.SKIN_PROPERTIES_READ_ONLY_ERROR));
        }

        parent.setValue(base, property, value);
    }
}
