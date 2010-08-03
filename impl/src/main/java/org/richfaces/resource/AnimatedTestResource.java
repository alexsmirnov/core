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
import java.awt.GraphicsEnvironment;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Node;

public class AnimatedTestResource extends TestResource2 {
    private static final int DELAY_TIME = 50;
    private static final int FRAMES_COUNT = 10;

    private static ImageWriter getImageWriter() {
        ImageWriter result = null;
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("gif");

        while (imageWriters.hasNext() && (result == null)) {
            ImageWriter imageWriter = imageWriters.next();

            if (imageWriter.canWriteSequence()) {
                result = imageWriter;
            }
        }

        return result;
    }

    private static Node getOrCreateChild(Node root, String name) {
        Node result = null;

        for (Node node = root.getFirstChild(); (node != null) && (result == null); node = node.getNextSibling()) {
            if (name.equals(node.getNodeName())) {
                result = node;
            }
        }

        if (result == null) {
            result = new IIOMetadataNode(name);
            root.appendChild(result);
        }

        return result;
    }

    @Override
    public String getContentType() {
        return "image/gif";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Dimension dimension = getDimension();
        BufferedImage image = new BufferedImage(dimension.width, dimension.height, ColorSpace.TYPE_RGB);
        Graphics2D g2d = environment.createGraphics(image);
        ImageWriter sequenceCapableImageWriter = getImageWriter();

        if (sequenceCapableImageWriter == null) {
            throw new IllegalStateException("No sequence-capable image writers exit");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = null;

        try {
            imageOutputStream = ImageIO.createImageOutputStream(baos);
            sequenceCapableImageWriter.setOutput(imageOutputStream);

            ImageWriteParam defaultImageWriteParam = sequenceCapableImageWriter.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(image.getType());
            IIOMetadata imageMetaData = sequenceCapableImageWriter.getDefaultImageMetadata(imageTypeSpecifier,
                defaultImageWriteParam);
            String metaFormatName = imageMetaData.getNativeMetadataFormatName();
            Node root = imageMetaData.getAsTree(metaFormatName);
            IIOMetadataNode graphicsControlExtensionNode = (IIOMetadataNode) getOrCreateChild(root,
                "GraphicControlExtension");

            // http://java.sun.com/javase/6/docs/api/javax/imageio/metadata/doc-files/gif_metadata.html
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(DELAY_TIME));
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

            boolean loopContinuously = false;
            Node applicationExtensionsNode = getOrCreateChild(root, "ApplicationExtensions");
            IIOMetadataNode netscapeExtension = new IIOMetadataNode("ApplicationExtension");

            netscapeExtension.setAttribute("applicationID", "NETSCAPE");
            netscapeExtension.setAttribute("authenticationCode", "2.0");

            byte numLoops = (byte) (loopContinuously ? 0x0 : 0x1);

            netscapeExtension.setUserObject(new byte[]{0x1, numLoops, 0x0});
            applicationExtensionsNode.appendChild(netscapeExtension);
            imageMetaData.setFromTree(metaFormatName, root);
            sequenceCapableImageWriter.prepareWriteSequence(null);

            for (int i = 1; i <= FRAMES_COUNT; i++) {
                g2d.setPaint(new GradientPaint(0, i * dimension.height / FRAMES_COUNT, Color.WHITE, 0,
                    dimension.height, getColor()));
                g2d.fillRect(0, 0, dimension.width, dimension.height);
                sequenceCapableImageWriter.writeToSequence(new IIOImage(image, null, imageMetaData),
                    defaultImageWriteParam);
            }

            sequenceCapableImageWriter.endWriteSequence();
        } catch (IOException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (imageOutputStream != null) {
                try {
                    imageOutputStream.close();
                } catch (IOException e) {

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            g2d.dispose();
            sequenceCapableImageWriter.dispose();
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }
}
