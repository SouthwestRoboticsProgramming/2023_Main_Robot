package com.swrobotics.messenger.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Allows easy storage of data into a message.
 *
 * @author rmheuer
 */
public final class MessageBuilder {
    private final MessengerClient client;
    private final String type;
    private final ByteArrayOutputStream b;
    private final DataOutputStream out;

    public MessageBuilder(MessengerClient client, String type) {
        this.client = client;
        this.type = type;
        b = new ByteArrayOutputStream();
        out = new DataOutputStream(b);
    }

    /** Sends the message with the type and data. */
    public void send() {
        client.sendMessage(type, b.toByteArray());
    }

    /**
     * Adds a {@code boolean} to this message.
     *
     * @param b boolean to add
     * @return this
     */
    public MessageBuilder addBoolean(boolean b) {
        try {
            out.writeBoolean(b);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write boolean", e);
        }
        return this;
    }

    /**
     * Adds a {@code String} to this message.
     *
     * @param s String to add
     * @return this
     */
    public MessageBuilder addString(String s) {
        try {
            MessengerClient.writeStringUtf8(out, s);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write String", e);
        }
        return this;
    }

    /**
     * Adds a {@code char} to this message.
     *
     * @param c char to add
     * @return this
     */
    public MessageBuilder addChar(char c) {
        try {
            out.writeChar(c);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write char", e);
        }
        return this;
    }

    /**
     * Adds a {@code byte} to this message.
     *
     * @param b byte to add
     * @return this
     */
    public MessageBuilder addByte(byte b) {
        try {
            out.writeByte(b);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write byte", e);
        }
        return this;
    }

    /**
     * Adds a {@code short} to this message.
     *
     * @param s short to add
     * @return this
     */
    public MessageBuilder addShort(short s) {
        try {
            out.writeShort(s);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write short", e);
        }
        return this;
    }

    /**
     * Adds an {@code int} to this message.
     *
     * @param i int to add
     * @return this
     */
    public MessageBuilder addInt(int i) {
        try {
            out.writeInt(i);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write int", e);
        }
        return this;
    }

    /**
     * Adds a {@code long} to this message.
     *
     * @param l long to add
     * @return this
     */
    public MessageBuilder addLong(long l) {
        try {
            out.writeLong(l);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write long", e);
        }
        return this;
    }

    /**
     * Adds a {@code float} to this message.
     *
     * @param f float to add
     * @return this
     */
    public MessageBuilder addFloat(float f) {
        try {
            out.writeFloat(f);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write float", e);
        }
        return this;
    }

    /**
     * Adds a {@code double} to this message.
     *
     * @param d double to add
     * @return this
     */
    public MessageBuilder addDouble(double d) {
        try {
            out.writeDouble(d);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write double", e);
        }
        return this;
    }

    /**
     * Adds raw data to this message.
     *
     * @param b data to add
     * @return self
     */
    public MessageBuilder addRaw(byte[] b) {
        try {
            out.write(b);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write raw data", e);
        }
        return this;
    }

    public byte[] getData() {
        return b.toByteArray();
    }
}
