package com.swrobotics.shufflelog.tool.field.path.grid;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

import java.util.BitSet;
import java.util.UUID;

public final class BitfieldGrid extends Grid {
    private int width;
    private int height;
    private BitSet data;

    public BitfieldGrid(UUID id) {
        super(id);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean get(int x, int y) {
        return data.get(x + y * width);
    }

    @Override
    public void readContent(MessageReader reader) {
        width = reader.readInt();
        height = reader.readInt();
        int len = reader.readInt();
        long[] l = new long[len];
        for (int i = 0; i < len; i++) {
            l[i] = reader.readLong();
        }
        data = BitSet.valueOf(l);
    }

    @Override
    public void write(MessageBuilder builder) {
        super.write(builder);
        builder.addByte(BITFIELD);
    }
}
