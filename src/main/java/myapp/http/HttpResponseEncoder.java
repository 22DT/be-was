package myapp.http;

import myapp.network.Connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseEncoder {
    private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.US_ASCII);

    public static void write(DataOutputStream dos, HttpResponse response) {
        try {
            writeStatusLine(dos, response);
            writeHeaders(dos, response);
            writeBody(dos, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeStatusLine(DataOutputStream dos, HttpResponse res)
            throws IOException {
        dos.writeBytes(
                "HTTP/1.1 " + res.getStatusCode() + " " + res.getMessage() + "\r\n"
        );
    }

    private static void writeHeaders(DataOutputStream dos, HttpResponse res)
            throws IOException {
        for (Map.Entry<String, String> h : res.getHeaders().entrySet()) {
            dos.writeBytes(h.getKey() + ": " + h.getValue() + "\r\n");
        }
        dos.writeBytes("\r\n");
    }

    private static void writeBody(DataOutputStream dos, HttpResponse res)
            throws IOException {
        if (res.getBody() != null) {
            dos.write(res.getBody());
        }
        dos.flush();
    }

    public static void write(Connection connection, HttpResponse response)
            throws IOException {

        //  Header 전용 버퍼 (충분히 작음)
        ByteBuffer headerBuffer = ByteBuffer.allocate(4096);

        writeStatusLine(headerBuffer, response);
        writeHeaders(headerBuffer, response);

        headerBuffer.flip();
        connection.write(headerBuffer);

        // Body는 별도 전송
        if (response.getBody() != null && response.getBody().length > 0) {
            connection.write(ByteBuffer.wrap(response.getBody()));
        }
    }

    private static void writeStatusLine(ByteBuffer buffer, HttpResponse res) {
        putAscii(
                buffer,
                "HTTP/1.1 " + res.getStatusCode() + " " + res.getMessage()
        );
        buffer.put(CRLF);
    }

    private static void writeHeaders(ByteBuffer buffer, HttpResponse res) {
        for (Map.Entry<String, String> h : res.getHeaders().entrySet()) {
            putAscii(buffer, h.getKey());
            putAscii(buffer, ": ");
            putAscii(buffer, h.getValue());
            buffer.put(CRLF);
        }
        buffer.put(CRLF);
    }

    private static void writeBody(ByteBuffer buffer, HttpResponse res) {
        if (res.getBody() != null) {
            buffer.put(res.getBody());
        }
    }

    private static void putAscii(ByteBuffer buffer, String s) {
        buffer.put(s.getBytes(StandardCharsets.US_ASCII));
    }

    public static ByteBuffer encodeHeaders(HttpResponse res) {
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        writeStatusLine(buffer, res);
        writeHeaders(buffer, res);

        buffer.flip();
        return buffer;
    }

    public static ByteBuffer encodeBody(HttpResponse res) {
        if (res.getBody() == null || res.getBody().length == 0) {
            return null;
        }
        return ByteBuffer.wrap(res.getBody());
    }

}


