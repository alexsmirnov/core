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

package org.ajax4jsf.context;

import java.util.concurrent.ConcurrentMap;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.ajax4jsf.util.ELUtils;
import org.richfaces.application.ServiceTracker;

/**
 * This class hold all methods for get application init parameters. Created for
 * single access point to all parameters - simplest for a documentation.
 *
 * @author asmirnov
 */
public final class ContextInitParameters {
    public static final String[] DATATABLE_USES_VIEW_LOCALE = {"org.richfaces.datatableUsesViewLocale"};

    /**
     * This parameter define where {@link javax.faces.application.ViewExpiredException} should be handled.
     * If is it equals "true" , framework should proparate exception to client-side.
     */
    public static final String HANDLE_VIEW_EXPIRED_ON_CLIENT = "org.ajax4jsf.handleViewExpiredOnClient";
    public static final String STD_CONTROLS_SKINNING_PARAM = "org.richfaces.ENABLE_CONTROL_SKINNING";
    public static final String STD_CONTROLS_SKINNING_CLASSES_PARAM = "org.richfaces.ENABLE_CONTROL_SKINNING_CLASSES";
    public static final String[] QUEUE_ENABLED = {"org.richfaces.queue.enabled"};

    private static final String INIT_PARAM_PREFIX = ContextInitParameters.class.getSimpleName() + ":";
    private static final Object NULL = new Object() {

        public String toString() {
            return ContextInitParameters.class.getSimpleName() + ": null Object";
        };
        
    };

    /**
     *
     */
    private ContextInitParameters() {

        // this is a only static methods for a access to Web app Init
        // parameters. Do not Instantiate !
    }

    /**
     * Defines whether data table should use view root locale for sorting and filtering or the
     * default platform one
     *
     * @param context
     * @return
     */
    public static boolean isDatatableUsesViewLocale(FacesContext context) {
        return getBoolean(context, DATATABLE_USES_VIEW_LOCALE, false);
    }
    
    public static boolean isQueueEnabled(FacesContext context) {
        return getBoolean(context, QUEUE_ENABLED, true);
    }
    
    /**
     * Method for determining STD_CONTROLS_SKINNING_PARAM parameter
     *
     * @param context
     * @return value of STD_CONTROLS_SKINNING_PARAM parameter if present.
     */
    public static boolean isStandardControlSkinningEnabled(FacesContext context) {
        String paramValue = evaluateInitParameter(context, STD_CONTROLS_SKINNING_PARAM);
        return getBooleanValue(paramValue, true);
    }
    
    /**
     * Method for determining STD_CONTROLS_SKINNING_CLASSES_PARAM parameter
     *
     * @param context
     * @return value of STD_CONTROLS_SKINNING_CLASSES_PARAM parameter if present.
     */
    public static boolean isStandardControlSkinningClassesEnabled(FacesContext context) {
        String paramValue = evaluateInitParameter(context, STD_CONTROLS_SKINNING_CLASSES_PARAM);
        return getBooleanValue(paramValue, false);
    }
    
    static int getInteger(FacesContext context, String[] paramNames, int defaulValue) {
        String initParameter = getInitParameter(context, paramNames);

        if (null == initParameter) {
            return defaulValue;
        } else {
            try {
                return Integer.parseInt(initParameter);
            } catch (NumberFormatException e) {
                throw new FacesException("Context parameter " + paramNames + " must have integer value");
            }
        }
    }

    static String getString(FacesContext context, String[] paramNames, String defaulValue) {
        String initParameter = getInitParameter(context, paramNames);

        if (null == initParameter) {
            return defaulValue;
        } else {
            return initParameter;
        }
    }

    static boolean getBoolean(FacesContext context, String[] paramNames, boolean defaulValue) {
        String initParameter = getInitParameter(context, paramNames);

        if (null == initParameter) {
            return defaulValue;
        } else if ("true".equalsIgnoreCase(initParameter) || "yes".equalsIgnoreCase(initParameter)) {
            return true;
        } else if ("false".equalsIgnoreCase(initParameter) || "no".equalsIgnoreCase(initParameter)) {
            return false;
        } else {
            throw new FacesException("Illegal value [" + initParameter + "] for a init parameter +" + paramNames
                + ", only logical values 'true' or 'false' is allowed");
        }
    }

    static String getInitParameter(FacesContext context, String[] paramNames) {
        ExternalContext externalContext = context.getExternalContext();
        String value = null;

        for (int i = 0; (i < paramNames.length) && (null == value); i++) {
            value = externalContext.getInitParameter(paramNames[i]);
        }

        return value;
    }
    
    private static boolean getBooleanValue(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }

        String stringValue = (String) value;
        
        return ("true".equalsIgnoreCase(stringValue) || "yes".equalsIgnoreCase(stringValue));
    }
    
    private static ConcurrentMap<Object, Object> getExpressionsMap(FacesContext context) {
        ConcurrentMap<Object, Object> concurrentStorage = ServiceTracker.getConcurrentStorage(context);
        return concurrentStorage;
    }
    
    private static String evaluateInitParameter(FacesContext context, String parameterName) {
        ConcurrentMap<Object, Object> expressionsMap = getExpressionsMap(context);
        String parameterKey = INIT_PARAM_PREFIX + parameterName;
        
        Object parameterValue = expressionsMap.get(parameterKey);
        if (parameterValue == null) {
            String initParameter = context.getExternalContext().getInitParameter(parameterName);
            if (initParameter != null) {
                
                if (ELUtils.isValueReference(initParameter)) {
                    Application application = context.getApplication();
                    ExpressionFactory expressionFactory = application.getExpressionFactory();
                    
                    parameterValue = expressionFactory.createValueExpression(context.getELContext(), 
                            initParameter,
                            String.class);
                } else {
                    parameterValue = initParameter;
                }
                
            } else {
                parameterValue = NULL;
            }
            
            expressionsMap.put(parameterKey, parameterValue);
        }
            
        return evaluateInitParameterExpression(context, parameterValue);
    }
    
    private static String evaluateInitParameterExpression(FacesContext context, Object parameterValue) {
        if (parameterValue == NULL || parameterValue == null) {
            return null;
        } else if (parameterValue instanceof ValueExpression) {
            ValueExpression expression = (ValueExpression) parameterValue;
            
            return (String) expression.getValue(context.getELContext());
        } else {
            return parameterValue.toString();
        }
    }

}