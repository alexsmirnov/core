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

package org.richfaces.resource;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.richfaces.util.Util;

final class DefaultResourceCodec implements ResourceCodec {

    private static final String RESOURCE_VERSION_PARAM = "v";

    private static final String RESOURCE_DATA_BYTES_ARRAY_PARAM = "db";
    
    private static final String RESOURCE_DATA_OBJECT_PARAM = "do";

    private static final ResourceCodec CODEC = new DefaultResourceCodec();

    private DefaultResourceCodec() {
    }

    public String decodeResourceName(FacesContext context, String requestPath) {
        return requestPath;
    }

    public Object decodeResourceData(FacesContext context, String requestPath) {
        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
        
        String encodedData = paramMap.get(RESOURCE_DATA_BYTES_ARRAY_PARAM);
        if (encodedData != null) {
            return Util.decodeBytesData(encodedData);
        } else {
            encodedData = paramMap.get(RESOURCE_DATA_OBJECT_PARAM);
            if (encodedData != null) {
                return Util.decodeObjectData(encodedData);
            }
        }
        
        return null;
    }

    public static ResourceCodec getInstance() {
        return CODEC;
    }

    public String decodeResourceVersion(FacesContext context, String requestPath) {
        return context.getExternalContext().getRequestParameterMap().get(RESOURCE_VERSION_PARAM);
    }

    private String encodeResource(FacesContext context, String resourceName, String encodedResourceData, 
        boolean dataIsBytesArray, String resourceVersion) {
        
        boolean parameterAppended = false;
        
        StringBuilder sb = new StringBuilder();
        sb.append(resourceName);
        
        if (resourceVersion != null && resourceVersion.length() != 0) {
            if (!parameterAppended) {
                sb.append('?');
                parameterAppended = true;
            }
            
            sb.append(RESOURCE_VERSION_PARAM);
            sb.append('=');
            sb.append(Util.encodeURIQueryPart(resourceVersion));
        }
        
        if (encodedResourceData != null && encodedResourceData.length() != 0) {
            if (!parameterAppended) {
                sb.append('?');
                parameterAppended = true;
            } else {
                sb.append('&');
            }
            
            sb.append(dataIsBytesArray ? RESOURCE_DATA_BYTES_ARRAY_PARAM : RESOURCE_DATA_OBJECT_PARAM);
            sb.append('=');
            sb.append(Util.encodeURIQueryPart(encodedResourceData));
        }

        return sb.toString();
    }
    
    public String encodeResource(FacesContext context, String resourceName, Object resourceData, String resourceVersion) {
        String encodedDataString = null;
        boolean isBytesArray = false;
        if (resourceData != null) {
            if (resourceData instanceof byte[]) {
                isBytesArray = true;
                encodedDataString = Util.encodeBytesData((byte[]) resourceData);
            } else {
                encodedDataString = Util.encodeObjectData(resourceData);
            }
        }

        return encodeResource(context, resourceName, encodedDataString, isBytesArray, resourceVersion);
    }
    
    public String getResourceKey(FacesContext context, String requestPath) {
        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
        
        boolean isBytesArray = true;
        String resourceDataString = paramMap.get(RESOURCE_DATA_BYTES_ARRAY_PARAM);
        if (resourceDataString == null) {
            resourceDataString = paramMap.get(RESOURCE_DATA_OBJECT_PARAM);
            isBytesArray = false;
        }
        
        String resourceVersionString = paramMap.get(RESOURCE_VERSION_PARAM);
        
        return encodeResource(context, decodeResourceName(context, requestPath), 
            resourceDataString, isBytesArray, resourceVersionString);
    }
}
