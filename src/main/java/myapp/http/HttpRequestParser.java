package myapp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpRequestParser {
    static final int MAX_BODY_SIZE = 1 * 1024 * 1024;

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

        // /CR / LF 가 라인 내부에 있으면 즉시 거부
        if (requestLine.indexOf('\r') != -1 || requestLine.indexOf('\n') != -1) {
            throw new IllegalArgumentException("Invalid CR/LF in request line");
        }

        // SP는 정확히 2개여야 함
        long count = requestLine.chars().filter((c) -> c == ' ').count();

        if (count != 2) {
            throw new IllegalArgumentException("Invalid HTTP request line: expected exactly 2 spaces, found " + count);
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid HTTP request line: expected 3 parts separated by single spaces, found " + parts.length);
        }

        // method

        HttpMethod method = HttpMethod.of(parts[0]);
        if (method == null) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + parts[0]);
        }

        String url = parts[1];


        // url

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

        // HTTP-Version 검증
        String version = parts[2];

        if (!version.matches("HTTP/\\d\\.\\d")) {
            throw new IllegalArgumentException("Invalid HTTP version: " + version);
        }

        /*
         * 2. Headers
         * */

        Map<String, String> headers = new HashMap<>();

        while (true) {
            String line = readLine(buffer);
            if (line == null) {
                buffer.reset();
                return null;
            }

            // 빈 줄 → 헤더 종료
            if (line.isEmpty()) {
                break;
            }

            // CR/LF 내부 금지
            if (line.indexOf('\r') != -1 || line.indexOf('\n') != -1) {
                throw new IllegalArgumentException("Invalid CR/LF in header");
            }

            int idx = line.indexOf(':');
            if (idx <= 0) {
                throw new IllegalArgumentException("Invalid header field: " + line);
            }

            String name = line.substring(0, idx);
            String value = line.substring(idx + 1);

            // 대소문자 무시를 위한 정규화
            name = name.toLowerCase(Locale.ROOT);
            value = value.trim();

            headers.put(name, value);
        }

        buffer.mark(); // body 시작 위치

        /*
         * 3. Body
         * */

        byte[] body = null;
        String cl = headers.get(HttpHeader.CONTENT_LENGTH.lower());

        if (cl != null && !cl.isEmpty()) {
            int contentLength;

            try {
                contentLength = Integer.parseInt(cl);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid Content-Length");
            }

            if (contentLength < 0 || contentLength > MAX_BODY_SIZE) {
                throw new IllegalArgumentException("Invalid Content-Length");
            }

            if (buffer.remaining() < contentLength) {
                System.out.println("뭔데?");
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


    public static HttpRequest parseHeader(ByteBuffer buffer) {
        buffer.mark();

        /*
         * 1. Request Line
         * */
        String requestLine = readLine(buffer);
        if (requestLine == null) {
            buffer.reset();
            return null;
        }

        // /CR / LF 가 라인 내부에 있으면 즉시 거부
        if (requestLine.indexOf('\r') != -1 || requestLine.indexOf('\n') != -1) {
            throw new IllegalArgumentException("Invalid CR/LF in request line");
        }

        // SP는 정확히 2개여야 함
        long count = requestLine.chars().filter((c) -> c == ' ').count();

        if (count != 2) {
            throw new IllegalArgumentException("Invalid HTTP request line: expected exactly 2 spaces, found " + count);
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid HTTP request line: expected 3 parts separated by single spaces, found " + parts.length);
        }

        // method

        HttpMethod method = HttpMethod.of(parts[0]);
        if (method == null) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + parts[0]);
        }

        String url = parts[1];


        // url

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

        // HTTP-Version 검증
        String version = parts[2];

        if (!version.matches("HTTP/\\d\\.\\d")) {
            throw new IllegalArgumentException("Invalid HTTP version: " + version);
        }

        /*
         * 2. Headers
         * */

        Map<String, String> headers = new HashMap<>();

        while (true) {
            String line = readLine(buffer);
            if (line == null) {
                buffer.reset();
                return null;
            }

            // 빈 줄 → 헤더 종료
            if (line.isEmpty()) {
                break;
            }

            // CR/LF 내부 금지
            if (line.indexOf('\r') != -1 || line.indexOf('\n') != -1) {
                throw new IllegalArgumentException("Invalid CR/LF in header");
            }

            int idx = line.indexOf(':');
            if (idx <= 0) {
                throw new IllegalArgumentException("Invalid header field: " + line);
            }

            String name = line.substring(0, idx);
            String value = line.substring(idx + 1);

            // 대소문자 무시를 위한 정규화
            name = name.toLowerCase(Locale.ROOT);
            value = value.trim();

            headers.put(name, value);
        }

        return new HttpRequest(method, path, queryParams, version, headers);
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
