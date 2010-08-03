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

import javax.faces.context.FacesContext;

import org.richfaces.util.Util;

final class LegacyResourceCodec implements ResourceCodec {

    private static final ResourceCodec CODEC = new LegacyResourceCodec();

    private LegacyResourceCodec() {
    }

    public String decodeResourceName(FacesContext context, String requestPath) {
        return Util.legacyDecodeResourceName(requestPath);
    }

    public Object decodeResourceData(FacesContext context, String requestPath) {
        return Util.legacyDecodeResourceData(requestPath);
    }

    public static ResourceCodec getInstance() {
        return CODEC;
    }

    public String decodeResourceVersion(FacesContext context, String requestPath) {
        return Util.legacyDecodeResourceVersion(requestPath);
    }

    public String encodeResource(FacesContext context, String resourceName, Object resourceData, String resourceVersion) {
        return Util.legacyEncodeResourceData(resourceName, resourceData, resourceVersion);
    }

    public String getResourceKey(FacesContext context, String requestPath) {
        return requestPath;
    }
}

