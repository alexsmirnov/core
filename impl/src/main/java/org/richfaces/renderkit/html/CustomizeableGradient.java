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

package org.richfaces.renderkit.html;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.resource.Java2Dresource;
import org.ajax4jsf.util.NumericDataInputStream;
import org.richfaces.renderkit.html.images.GradientAlignment;
import org.richfaces.renderkit.html.images.GradientType;
import org.richfaces.renderkit.html.images.GradientType.BiColor;
import org.richfaces.resource.DynamicResource;
import org.richfaces.skin.Skin;

/**
 * @author Nick Belaevski - nbelaevski@exadel.com
 *         created 02.02.2007
 */
@DynamicResource
public class CustomizeableGradient extends Java2Dresource {

    private static final String BASE_COLOR = "baseColor";

    private static final String GRADIENT_COLOR = "gradientColor";

    protected Dimension dimension = new Dimension(20, 500);

    protected Integer gradientColor;
    protected Integer baseColor;
    protected Integer gradientHeight = 22;

    protected GradientType gradientType;
    protected GradientAlignment gradientAlignment;

    public CustomizeableGradient() {
        super(ImageType.PNG);

        String gradientTypeString = safeTrim(getValueParameter(FacesContext.getCurrentInstance(), Skin.GRADIENT_TYPE));
        if (gradientTypeString != null && gradientTypeString.length() != 0) {
            gradientType = GradientType.getByParameter(gradientTypeString);
        }
    }
    
    @Override
    public void populateParameters(Map<String, String> parameters){
        if(parameters.containsKey(GRADIENT_COLOR)){
            String gradientColorValue = encodeSkinParameter(parameters.get(GRADIENT_COLOR));
            this.gradientColor = decodeColor(gradientColorValue);
        }
        if(parameters.containsKey(BASE_COLOR)){
            String baseColorValue = encodeSkinParameter(parameters.get(BASE_COLOR));
            this.baseColor = decodeColor(baseColorValue);
        }
    }
    
    public Dimension getDimension() {
        return dimension;
    }

    private void drawRectangle(Graphics2D g2d, Rectangle2D rect, BiColor biColor, boolean useTop) {
        if (biColor != null) {
            Color color = useTop ? biColor.getTopColor() : biColor.getBottomColor();
            g2d.setColor(color);
            g2d.fill(rect);
        }
    }

    private void drawGradient(Graphics2D g2d, Rectangle2D rectangle, BiColor colors, int height) {
        if (colors != null) {
            GradientPaint gragient = new GradientPaint(0, 0, colors.getTopColor(), 0, height, colors.getBottomColor());
            g2d.setPaint(gragient);
            g2d.fill(rectangle);
        }
    }

    protected void paint(Graphics2D g2d, Dimension dim) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if ((gradientColor != null || baseColor != null) && gradientType != null
            && gradientAlignment != null) {

            paintGradient(g2d, dim);
        }
    }

    protected void paintGradient(Graphics2D g2d, Dimension dim) {
        BiColor biColor = new GradientType.BiColor(gradientColor, baseColor);
        BiColor firstLayer = gradientType.getFirstLayerColors(biColor);
        BiColor secondLayer = gradientType.getSecondLayerColors(biColor);

        Rectangle2D rect =
            new Rectangle2D.Float(
                0,
                0,
                dim.width,
                dim.height);

        int topRectangleHeight = gradientAlignment.getTopRectangleHeight(dim.height, gradientHeight);
        int bottomRectangleHeight = gradientAlignment.getBottomRectangleHeight(dim.height, gradientHeight);

        rect = new Rectangle2D.Float(
            0,
            0,
            dim.width,
            topRectangleHeight);

        drawRectangle(g2d, rect, firstLayer, true);
        drawRectangle(g2d, rect, secondLayer, true);

        rect = new Rectangle2D.Float(
            0,
            dim.height - bottomRectangleHeight,
            dim.width,
            dim.height);

        drawRectangle(g2d, rect, firstLayer, false);
        drawRectangle(g2d, rect, secondLayer, false);

        g2d.transform(AffineTransform.getTranslateInstance(0, topRectangleHeight));

        rect = new Rectangle2D.Float(
            0,
            0,
            dim.width,
            dim.height);

        drawGradient(g2d, rect, firstLayer, gradientHeight);

        rect = new Rectangle2D.Float(
            0,
            0,
            dim.width,
            gradientHeight / 2);

        drawGradient(g2d, rect, secondLayer, gradientHeight / 2);
    }

    @Override
    protected void readState(FacesContext context, NumericDataInputStream stream) {
        super.readState(context, stream);

        this.baseColor = stream.readIntColor();
        this.gradientColor = stream.readIntColor();
        this.gradientHeight = stream.readInt();
        this.gradientAlignment = GradientAlignment.values()[stream.readByte()];
        this.gradientType = GradientType.values()[stream.readByte()];
    }
    @Override
    protected void writeState(FacesContext context, org.ajax4jsf.util.NumericDataOutputStream stream) {
        super.writeState(context, stream);

        stream.writeIntColor(this.baseColor);
        stream.writeIntColor(this.gradientColor);
        stream.writeInt(this.gradientHeight);
        stream.writeByte((byte) this.gradientAlignment.ordinal());
        stream.writeByte((byte) this.gradientType.ordinal());
    }

    protected static String safeTrim(String s) {
        return s != null ? s.trim() : null;
    }

    public boolean isCacheable() {
        return true;
    }

    public Integer getGradientColor() {
        return gradientColor;
    }

    public void setGradientColor(Integer headerBackgroundColor) {
        this.gradientColor = headerBackgroundColor;
    }

    public void setGradientColorString(String colorString) {
        setGradientColor(decodeColor(colorString));
    }

    public Integer getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(Integer headerGradientColor) {
        this.baseColor = headerGradientColor;
    }

    public void setBaseColorString(String colorString) {
        setBaseColor(decodeColor(colorString));
    }

    public GradientType getGradientType() {
        return gradientType;
    }

    public void setGradientType(GradientType gradientType) {
        this.gradientType = gradientType;
    }

    public GradientAlignment getGradientAlignment() {
        return gradientAlignment;
    }

    public void setGradientAlignment(GradientAlignment gradientAlignment) {
        this.gradientAlignment = gradientAlignment;
    }

    public Integer getGradientHeight() {
        return gradientHeight;
    }

    public void setGradientHeight(Integer gradientHeight) {
        this.gradientHeight = gradientHeight;
    }

}
