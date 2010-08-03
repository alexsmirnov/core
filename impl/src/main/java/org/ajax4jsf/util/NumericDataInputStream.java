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

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Original idea by Igor Shabalov
 *
 * @author Nick Belaevski
 * @since 4.0
 */
public class NumericDataInputStream {
    static final int BYTES_IN_INT = Integer.SIZE >> 3;
    static final int BYTES_IN_SHORT = Short.SIZE >> 3;
    static final int BYTES_IN_COLOR = BYTES_IN_INT - 1;

    // the size of maximum object in bytes that this stream can operate (int)
    static final int MAX_BYTES = BYTES_IN_INT;
    static final ByteOrder BUFFER_BYTES_ORDER = ByteOrder.LITTLE_ENDIAN;
    private byte[] bytes = new byte[MAX_BYTES];
    private ByteBuffer buffer = ByteBuffer.wrap(bytes).order(BUFFER_BYTES_ORDER);
    private ByteArrayInputStream byteArrayStream;

    public NumericDataInputStream(byte[] buf) {
        super();
        byteArrayStream = new ByteArrayInputStream(buf);
    }

    public NumericDataInputStream(byte[] buf, int offset, int length) {
        super();
        byteArrayStream = new ByteArrayInputStream(buf, offset, length);
    }

    public byte readByte() {
        int read = byteArrayStream.read();

        if (read >= 0) {
            return (byte) read;
        } else {
            throw new IllegalStateException("Data is invalid or corrupted");
        }
    }

    public short readShort() {
        int read = byteArrayStream.read(bytes, 0, BYTES_IN_SHORT);

        if (read == BYTES_IN_SHORT) {
            buffer.rewind();

            return buffer.asShortBuffer().get();
        } else {
            throw new IllegalStateException("Data is invalid or corrupted");
        }
    }

    public int readInt() {
        int read = byteArrayStream.read(bytes, 0, BYTES_IN_INT);

        if (read == BYTES_IN_INT) {
            buffer.rewind();

            return buffer.asIntBuffer().get();
        } else {
            throw new IllegalStateException("Data is invalid or corrupted");
        }
    }

    public int readIntColor() {
        int read = byteArrayStream.read(bytes, 0, BYTES_IN_COLOR);

        if (read == BYTES_IN_COLOR) {
            buffer.rewind();

            return buffer.asIntBuffer().get() & 0x00FFFFFF;
        } else {
            throw new IllegalStateException("Data is invalid or corrupted");
        }
    }

    public Color readColor() {
        return new Color(readIntColor());
    }
}
