package dragon_tiger.network;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 읽거나 쓴 바이트 수 반환함.
 */
public interface Connection {
    int read(ByteBuffer dst) throws IOException;

    int write(ByteBuffer src) throws IOException;

    void close() throws IOException;
}
