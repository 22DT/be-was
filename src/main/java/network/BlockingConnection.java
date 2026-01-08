package network;

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
        } catch (IOException ignore) {}

        try {
            in.close();
        } catch (IOException ignore) {}

        socket.close();
    }

    public Socket getSocket(){
        return this.socket;
    }
}

