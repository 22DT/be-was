package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class HttpResponseWriter {

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
}

