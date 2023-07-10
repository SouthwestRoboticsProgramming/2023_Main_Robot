package com.swrobotics.messenger.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Allows easy access to data stored within a message.
 *
 * @author rmheuer
 */
public final class MessageReader {
    private final DataInputStream in;
    private boolean closed;

    /**
     * Creates a new MessageReader that reads from a raw byte array.
     *
     * @param data raw data
     */
    public MessageReader(byte[] data) {
        in = new DataInputStream(new ByteArrayInputStream(data));
        closed = false;
    }

    /**
     * Reads a {@code boolean} from the message.
     *
     * @return boolean read
     */
    public boolean readBoolean() {
        try {
            return in.readBoolean();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read boolean", e);
        }
    }

    /**
     * Reads a {@code String} from the message.
     *
     * @return String read
     */
    public String readString() {
        try {
            return MessengerClient.readStringUtf8(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read String", e);
        }
    }

    /**
     * Reads an {@code char} from the message.
     *
     * @return char read
     */
    public char readChar() {
        try {
            return in.readChar();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read char", e);
        }
    }

    /**
     * Reads a {@code byte} from the message.
     *
     * @return byte read
     */
    public byte readByte() {
        try {
            return in.readByte();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read byte", e);
        }
    }

    /**
     * Reads a {@code short} from the message.
     *
     * @return short read
     */
    public short readShort() {
        try {
            return in.readShort();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read short", e);
        }
    }

    /**
     * Reads an {@code int} from the message.
     *
     * @return int read
     */
    public int readInt() {
        try {
            return in.readInt();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read int", e);
        }
    }

    /**
     * Reads a {@code long} from the message.
     *
     * @return long read
     */
    public long readLong() {
        try {
            return in.readLong();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read long", e);
        }
    }

    /**
     * Reads a {@code float} from the message.
     *
     * @return float read
     */
    public float readFloat() {
        try {
            return in.readFloat();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read float", e);
        }
    }

    /**
     * Reads a {@code double} from the message.
     *
     * @return double read
     */
    public double readDouble() {
        try {
            return in.readDouble();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read double", e);
        }
    }

    /**
     * Reads raw data as a byte array from the message.
     *
     * @param length number of bytes to read
     * @return bytes read
     */
    public byte[] readRaw(int length) {
        try {
            byte[] buf = new byte[length];
            in.readFully(buf);
            return buf;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data", e);
        }
    }

    /**
     * Reads all remaining data as a {@code byte} array.
     *
     * @return data read
     */
    public byte[] readAllData() {
        try {
            return readRaw(in.available());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read data", e);
        }
    }

    /** Closes the reader when done reading. */
    public void close() {
        if (!closed) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close reader", e);
            }

            closed = true;
        }
    }
}
