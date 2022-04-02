package com.plexobject.deps;

import java.io.DataInputStream;
import java.io.IOException;

public class BytecodeParser {
    private DataInputStream in;

    public BytecodeParser(DataInputStream in) {
        this.in = in;
    }

    public int u1() throws IOException {
        return in.readUnsignedByte();
    }

    public int u2() throws IOException {
        return in.readUnsignedShort();
    }
}
