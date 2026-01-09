package http;

import myapp.network.Connection;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

class FakeConnection implements Connection {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public int read(ByteBuffer dst) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer src) {
        int len = src.remaining();
        byte[] bytes = new byte[len];
        src.get(bytes);
        out.write(bytes, 0, len);
        return len;
    }

    @Override
    public void close() {
    }

    public byte[] getWrittenBytes() {
        return out.toByteArray();
    }
}

