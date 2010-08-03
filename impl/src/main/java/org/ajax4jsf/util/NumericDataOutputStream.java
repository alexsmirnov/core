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

package org.ajax4jsf.util;

import static org.ajax4jsf.util.NumericDataInputStream.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Original idea by Igor Shabalov
 *
 * @author Nick Belaevski
 * @since 4.0
 */
public class NumericDataOutputStream {
    private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    private byte[] bytes = new byte[MAX_BYTES];
    private ByteBuffer buffer = ByteBuffer.wrap(bytes).order(BUFFER_BYTES_ORDER);

    public NumericDataOutputStream writeByte(byte value) {
        byteStream.write(value);

        return this;
    }

    public NumericDataOutputStream writeShort(short value) {
        buffer.rewind();
        buffer.asShortBuffer().put(value);
        byteStream.write(bytes, 0, BYTES_IN_SHORT);

        return this;
    }

    private void writeInteger(int value, int numBytes) {
        buffer.rewind();
        buffer.asIntBuffer().put(value);
        byteStream.write(bytes, 0, numBytes);
    }

    public NumericDataOutputStream writeInt(int value) {
        writeInteger(value, BYTES_IN_INT);

        return this;
    }

    public NumericDataOutputStream writeIntColor(int value) {
        writeInteger(value, BYTES_IN_COLOR);

        return this;
    }

    public NumericDataOutputStream writeColor(Color value) {
        writeIntColor(value.getRGB());

        return this;
    }

    public byte[] getBytes() {
        return byteStream.toByteArray();
    }
}
