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

import java.awt.Color;
import java.util.Map;

import javax.el.Expression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import org.ajax4jsf.util.HtmlColor;

/**
 * Singleton ( in respect as collection of different skins ) for produce
 * instances properties for all used skins.
 *
 * @author shura (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:59:41 $
 */
public abstract class BasicSkinImpl implements Skin {
    
    public static final String RENDER_KIT_PARAMETER = "render.kit";
    
    private int hashCode = 0;
    
    private final Map<Object, Object> skinParams;

    /**
     * Skin can instantiate only by factory method.
     *
     * @param skinName
     */
    BasicSkinImpl(Map<Object, Object> properties) {
        this.skinParams = properties;
    }

    protected Map<Object, Object> getSkinParams() {
        return skinParams;
    }

    /*
     *  (non-Javadoc)
     * @see org.richfaces.skin.Skin#getRenderKitId(javax.faces.context.FacesContext)
     */
    public String getRenderKitId(FacesContext context) {
        return (String) getValueReference(context, resolveSkinParameter(context, RENDER_KIT_PARAMETER));
    }

    /*
     *  (non-Javadoc)
     * @see org.richfaces.skin.Skin#getParameter(javax.faces.context.FacesContext, java.lang.String)
     */
    public Object getParameter(FacesContext context, String name) {
        return getValueReference(context, resolveSkinParameter(context, name));
    }

    /*
     *  (non-Javadoc)
     * @see org.richfaces.skin.Skin#getParameter(javax.faces.context.FacesContext, java.lang.String, java.lang.String, java.lang.Object)
     */
    public Object getParameter(FacesContext context, String name, Object defaultValue) {
        Object value = getValueReference(context, resolveSkinParameter(context, name));

        if (null == value) {
            value = defaultValue;
        }

        return value;
    }

    protected Integer decodeColor(Object value) {
        if (value instanceof Color) {
            return ((Color) value).getRGB();
        } else if (value instanceof Integer) {
            return ((Integer) value).intValue();
        } else {
            String stringValue = (String) value;
            if (stringValue != null && stringValue.length() != 0) {
                return Integer.valueOf(HtmlColor.decode(stringValue).getRGB());
            } else {
                return null;
            }
        }
    }

    public Integer getColorParameter(FacesContext context, String name) {
        return decodeColor(getParameter(context, name));
    }
    
    public Integer getColorParameter(FacesContext context, String name, Object defaultValue) {
        return decodeColor(getParameter(context, name, defaultValue));
    }
    
    protected Object getLocalParameter(FacesContext context, String name) {
        return getValueReference(context, skinParams.get(name));
    }

    protected abstract Object resolveSkinParameter(FacesContext context, String name);

    /**
     * Calculate concrete value for property - if it stored as @see ValueBinding ,
     * return interpreted value.
     *
     * @param context
     * @param property
     * @return
     */
    protected Object getValueReference(FacesContext context, Object property) {
        if (property instanceof ValueExpression) {
            ValueExpression value = (ValueExpression) property;

            return value.getValue(context.getELContext());
        }

        return property;
    }

    public String toString() {
        return this.getClass().getSimpleName() + ": " + skinParams.toString();
    }

    protected int computeHashCode(FacesContext context) {
        int localHash = hashCode;
        if (localHash == 0) {
            boolean hasDynamicValues = false;

            for (Map.Entry<Object, Object> entry : skinParams.entrySet()) {
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof Expression) {
                    hasDynamicValues = true;
                }
                
                Object parameter = getValueReference(context, value);

                localHash = 31 * localHash + key.hashCode();
                localHash = 31 * localHash + ((parameter != null) ? parameter.hashCode() : 0);
            }
            
            if (!hasDynamicValues) {
                hashCode = localHash;
            }
        }

        return localHash;
    }

    /*
     *  (non-Javadoc)
     * @see org.richfaces.skin.Skin#hashCode(javax.faces.context.FacesContext)
     */
    public int hashCode(FacesContext context) {
        if (hashCode != 0) {
            return hashCode;
        }
        
        Map<Object, Object> attributesMap = context.getAttributes();
        
        Integer requestCode = (Integer) attributesMap.get(this);

        if (null == requestCode) {
            requestCode = new Integer(computeHashCode(context));

            // store hash for this skin as request-skope parameter - not calculate on next calls for same request
            attributesMap.put(this, requestCode);
        }

        return requestCode.intValue();
    }
}
