/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2007  Exadel, Inc.
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

import java.util.Map;

import javax.faces.context.FacesContext;

/**
 * @author nick belaevski
 * @since 3.2
 */

public class BaseSkinImpl extends AbstractChainableSkinImpl {

    private SkinFactoryImpl factoryImpl;

    BaseSkinImpl(Map<Object, Object> properties, SkinFactoryImpl factory) {
        super(properties);

        this.factoryImpl = factory;
    }

    protected Skin getBaseSkin(FacesContext context) {
        Object object = getLocalParameter(context, Skin.BASE_SKIN);
        Skin skin = null;

        if (object != null) {
            skin = factoryImpl.getSkinByName(context, object, true);
        }

        if (skin == null) {
            skin = factoryImpl.getDefaultSkin(context);
        }

        return skin;
    }

}
