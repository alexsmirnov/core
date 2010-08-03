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



package org.ajax4jsf.util;

import java.awt.Color;

import junit.framework.TestCase;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class NumericStreamsTest extends TestCase {
    public void testByte() throws Exception {
        byte[] bytes = new NumericDataOutputStream().writeByte((byte) 0xDF).writeByte((byte) 0x90).writeByte(
                           (byte) 0xAA).getBytes();
        NumericDataInputStream inputStream = new NumericDataInputStream(bytes);

        assertEquals((byte) 0xDF, inputStream.readByte());
        assertEquals((byte) 0x90, inputStream.readByte());
        assertEquals((byte) 0xAA, inputStream.readByte());
    }

    public void testShort() throws Exception {
        byte[] bytes = new NumericDataOutputStream().writeShort((short) 0xA7DF).writeShort((short) 0xFE90).writeShort(
                           (short) 0x34AA).getBytes();
        NumericDataInputStream inputStream = new NumericDataInputStream(bytes);

        assertEquals((short) 0xA7DF, inputStream.readShort());
        assertEquals((short) 0xFE90, inputStream.readShort());
        assertEquals((short) 0x34AA, inputStream.readShort());
    }

    public void testColor() throws Exception {
        byte[] bytes = new NumericDataOutputStream().writeColor(new Color(0xA7DFE0)).writeIntColor(0xE2349A).writeColor(
                           new Color(0x4812F9)).getBytes();
        NumericDataInputStream inputStream = new NumericDataInputStream(bytes);

        assertEquals(0xA7DFE0, inputStream.readIntColor());
        assertEquals(new Color(0xE2349A), inputStream.readColor());
        assertEquals(0x4812F9, inputStream.readIntColor());
    }

    public void testInt() throws Exception {
        byte[] bytes =
            new NumericDataOutputStream().writeInt(0x12A7DFE0).writeInt(0x67E2349A).writeInt(0xBD4812F9).getBytes();
        NumericDataInputStream inputStream = new NumericDataInputStream(bytes);

        assertEquals(0x12A7DFE0, inputStream.readInt());
        assertEquals(0x67E2349A, inputStream.readInt());
        assertEquals(0xBD4812F9, inputStream.readInt());
    }
}
