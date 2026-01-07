package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {

    public static HttpRequest parse(InputStream in) throws IOException {

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(in));

        // 1. Request Line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IllegalArgumentException("Invalid HTTP request line");
        }

        String[] parts = requestLine.split(" ");
        HttpMethod method = HttpMethod.of(parts[0]);

        if (method == null) {
            throw new IllegalArgumentException(
                    "Unsupported HTTP method: " + method
            );
        }

        String url = parts[1];
        String version = parts[2];


        // 2. url 에서 path / query 분리
        String[] urlParts = url.split("\\?");
        String path = urlParts[0];

        Map<String, String> queryParams = new HashMap<>();

        if (urlParts.length == 2) {
            String queryString = urlParts[1];

            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                String key = kv[0];
                String value = (kv.length == 2) ? kv[1] : ""; // ex) /create?userId&pass <= 이런 예외
                queryParams.put(key, value);
            }
        }

        // 3. Headers
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                headers.put(key, value);
            }
        }

        // 4. Body
        byte[] body = null;
        String contentLengthValue = headers.get(HttpHeader.CONTENT_LENGTH.value());

        if (contentLengthValue != null && !contentLengthValue.isEmpty()) {
            int contentLength = Integer.parseInt(contentLengthValue);

            char[] bodyChars = new char[contentLength];
            int read = 0;

            while (read < contentLength) {
                int r = reader.read(bodyChars, read, contentLength - read);
                if (r == -1) break;
                read += r;
            }

            body = new String(bodyChars, 0, read).getBytes(StandardCharsets.UTF_8);
        }


        return new HttpRequest(method, path, queryParams, version, headers, body);
    }

    public static HttpRequest parse(ByteBuffer buffer) {
        buffer.mark();

        /*
         * 1. Request Line
         * */
        String requestLine = readLine(buffer);
        if (requestLine == null) {
            buffer.reset();
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid HTTP request line");
        }

        HttpMethod method = HttpMethod.of(parts[0]);
        if (method == null) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + parts[0]);
        }

        String url = parts[1];
        String version = parts[2];


        /*
         * 2. URL -> path / query
         * */

        String[] urlParts = url.split("\\?", 2);
        String path = urlParts[0];

        Map<String, String> queryParams = new HashMap<>();
        if (urlParts.length == 2) {
            for (String pair : urlParts[1].split("&")) {
                String[] kv = pair.split("=", 2);
                String key = kv[0];
                String value = (kv.length == 2) ? kv[1] : "";
                queryParams.put(key, value);
            }
        }


        /*
         * 3. Headers
         * */

        Map<String, String> headers = new HashMap<>();
        String line;

        while (true) {
            line = readLine(buffer);
            if (line == null) {
                buffer.reset();
                return null;
            }
            if (line.isEmpty()) break;

            int idx = line.indexOf(':');
            if (idx > 0) {
                headers.put(
                        line.substring(0, idx).trim(),
                        line.substring(idx + 1).trim()
                );
            }
        }

        /*
         * 4. Body
         * */

        byte[] body = null;
        String cl = headers.get(HttpHeader.CONTENT_LENGTH.value());

        if (cl != null && !cl.isEmpty()) {
            int contentLength = Integer.parseInt(cl);

            if (buffer.remaining() < contentLength) {
                buffer.reset();
                return null;
            }

            body = new byte[contentLength];
            buffer.get(body);
        }

        return new HttpRequest(
                method, path, queryParams, version, headers, body
        );
    }

    private static String readLine(ByteBuffer buffer) {
        int start = buffer.position();

        for (int i = start; i + 1 < buffer.limit(); i++) {
            if (buffer.get(i) == '\r' && buffer.get(i + 1) == '\n') {
                int len = i - start;
                byte[] bytes = new byte[len];
                buffer.get(bytes);
                buffer.get(); // \r
                buffer.get(); // \n
                return new String(bytes, StandardCharsets.US_ASCII);
            }
        }

        return null;
    }
}
