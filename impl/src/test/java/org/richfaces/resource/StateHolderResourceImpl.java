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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.faces.FacesException;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
@DynamicResource
public class StateHolderResourceImpl extends AbstractBaseResource implements StateHolder {
    private Object state = "";

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return new ByteArrayInputStream(state.toString().getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new FacesException(e.getMessage(), e);
        }
    }

    public boolean isTransient() {
        return false;
    }

    public void restoreState(FacesContext context, Object state) {
        this.state = state;
    }

    public Object saveState(FacesContext context) {
        return state;
    }

    public void setTransient(boolean newTransientValue) {
        throw new UnsupportedOperationException();
    }
}
