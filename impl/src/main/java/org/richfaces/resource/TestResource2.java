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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import javax.faces.context.FacesContext;

import org.ajax4jsf.resource.Java2Dresource;
import org.ajax4jsf.util.HtmlColor;
import org.ajax4jsf.util.NumericDataInputStream;
import org.ajax4jsf.util.NumericDataOutputStream;
import org.richfaces.VersionBean;
import org.richfaces.skin.Skin;
import org.richfaces.skin.SkinFactory;

@DynamicResource
public class TestResource2 extends Java2Dresource {

    private static final int MASK_FOR_COLOR_WITHOUT_ALPHA_CHANNEL = 0x00FFFFFF;

    private Color color;

    private Dimension dimension = new Dimension(20, 150);

    public TestResource2() {
        super(ImageType.PNG);
        FacesContext context = FacesContext.getCurrentInstance();
        Skin skin = SkinFactory.getInstance().getSkin(context);
        Object parameter = skin.getParameter(context, Skin.HEADER_GRADIENT_COLOR);
        this.color = HtmlColor.decode(parameter.toString());
    }

    @Override
    protected void paint(Graphics2D graphics2d, Dimension dim) {
        super.paint(graphics2d, dim);

        graphics2d.setPaint(new GradientPaint(0, 0, Color.WHITE, dim.width, dim.height, color));
        graphics2d.fillRect(0, 0, dim.width, dim.height);
    }

    @Override
    protected void readState(FacesContext context, NumericDataInputStream stream) {
        super.readState(context, stream);
        this.color = stream.readColor();
    }

    @Override
    protected void writeState(FacesContext context,
                              NumericDataOutputStream stream) {
        super.writeState(context, stream);
        stream.writeColor(this.color);
    }

    @Override
    public String getEntityTag(FacesContext context) {
        return ResourceUtils.formatWeakTag(getColorWitoutAlphaChanel());
    }

    private String getColorWitoutAlphaChanel() {
        return Integer.toHexString(color.getRGB() & MASK_FOR_COLOR_WITHOUT_ALPHA_CHANNEL);
    }

    @Override
    public String getVersion() {
        return VersionBean.VERSION.getResourceVersion();
    }

    /* (non-Javadoc)
      * @see org.ajax4jsf.resource.Java2Dresource#getDimension()
      */
    @Override
    public Dimension getDimension() {
        return dimension;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
