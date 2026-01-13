package myapp.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class BlockingConnection implements Connection {

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public BlockingConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        byte[] buf = new byte[dst.remaining()];
        int read = in.read(buf);
        if (read > 0) {
            dst.put(buf, 0, read);
        }
        return read; // -1 이면 EOF
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int len = src.remaining();
        byte[] buf = new byte[len];
        src.get(buf);
        out.write(buf);
        out.flush(); // blocking 서버에서는 flush가 안전
        return len;
    }

    @Override
    public void close() throws IOException {
        try {
            out.close();
        } catch (IOException ignore) {
        }

        try {
            in.close();
        } catch (IOException ignore) {
        }

        socket.close();
    }

    public Socket getSocket() {
        return this.socket;
    }

    public byte[] readExactly(ByteBuffer buffer, int length) throws IOException {

        byte[] body = new byte[length];
        int offset = 0;

        // 1. buffer에 이미 있는 데이터부터 소비
        int available = Math.min(buffer.remaining(), length);
        buffer.get(body, 0, available);
        offset += available;

        // 2. 부족하면 소켓에서 직접 읽기
        while (offset < length) {
            int read = read(ByteBuffer.wrap(body, offset, length - offset));

            if (read == -1) {
                throw new IOException("Unexpected EOF while reading body");
            }

            offset += read;
        }

        return body;
    }

}

