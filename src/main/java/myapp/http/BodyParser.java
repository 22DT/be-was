package myapp.http;

import myapp.file.UploadedFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class BodyParser {

    public static Map<String, String> getBodyParams(HttpRequest request) {
        String contentType = request.getHeader(HttpHeader.CONTENT_TYPE);

        if (contentType == null ||
                !contentType.startsWith("application/x-www-form-urlencoded")) {
            return Map.of();
        }

        byte[] body = request.getBody();
        if (body == null || body.length == 0) {
            return Map.of();
        }

        String bodyString = new String(body, StandardCharsets.UTF_8);
        HashMap<String, String> params = new HashMap<>();

        String[] pairs = bodyString.split("&");

        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = kv[0];
            String value = (kv.length == 2) ? kv[1] : "";

            params.put(key, value);
        }


        return params;
    }

    public static UploadedFile getMultipart(HttpRequest request) {
        String contentType = request.getHeader(HttpHeader.CONTENT_TYPE);

        if (contentType == null ||
                !contentType.startsWith("multipart/form-data")) {
            return null;
        }

        byte[] body = request.getBody();
        if (body == null || body.length == 0) {
            return null;
        }

        /*
         * boundary 추출
         *
         * boundary?
         *
         * HTTP 요청 바디 안에 여러 덩어리(텍스트 필드, 파일 등)가 들어 있을 때
         * "여기서부터 여기까지가 한 덩어리다 라고 경계선을 긋는 역할"
         * */

        int boundaryIndex = contentType.indexOf("boundary=");
        if (boundaryIndex == -1) {
            throw new IllegalArgumentException("No boundary in Content-Type");
        }

        String boundary = contentType
                .substring(boundaryIndex + 9)
                .trim();

        int semicolon = boundary.indexOf(';');
        if (semicolon != -1) {
            boundary = boundary.substring(0, semicolon);
        }

        byte[] delimiter = ("--" + boundary).getBytes(StandardCharsets.US_ASCII);

        List<byte[]> parts = split(body, delimiter);

        for (byte[] part : parts) {
            if (part.length == 0) continue;

            // 마지막 boundary(--boundary--) 제거
            if (endsWith(part, "--".getBytes(StandardCharsets.US_ASCII))) {
                continue;
            }

            int headerEnd = indexOf(
                    part,
                    "\r\n\r\n".getBytes(StandardCharsets.US_ASCII)
            );
            if (headerEnd == -1) continue;

            byte[] headerBytes = Arrays.copyOfRange(part, 0, headerEnd);
            byte[] data = Arrays.copyOfRange(
                    part,
                    headerEnd + 4,
                    trimCrlf(part)
            );

            String headers = new String(headerBytes, StandardCharsets.UTF_8);

            if (!headers.contains("Content-Disposition")) continue;

            String name = extract(headers, "name");
            String filename = extract(headers, "filename");
            if (filename == null) continue;

            String fileContentType = extractHeader(headers, "Content-Type");

            return new UploadedFile(name, filename, fileContentType, data);
        }

        return null;
    }

    private static int trimCrlf(byte[] part) {
        int end = part.length;

        if (end >= 2 && part[end - 2] == '\r' && part[end - 1] == '\n') {
            end -= 2;
        }
        return end;
    }

    private static boolean endsWith(byte[] src, byte[] suffix) {
        if (src.length < suffix.length) return false;

        for (int i = 0; i < suffix.length; i++) {
            if (src[src.length - suffix.length + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }


    private static List<byte[]> split(byte[] body, byte[] delimiter) {
        List<byte[]> result = new ArrayList<>();

        int pos = 0;
        while (true) {
            int idx = indexOf(body, delimiter, pos);
            if (idx == -1) break;

            int start = idx + delimiter.length - 2;
            int next = indexOf(body, delimiter, start);

            if (next == -1) break;

            result.add(Arrays.copyOfRange(body, start, next));
            pos = next;
        }
        return result;
    }

    /**
     * byte[] 안에서 다른 byte[] 패턴이 처음 등장하는 위치를 찾는 코드
     */
    private static int indexOf(byte[] src, byte[] target) {
        return indexOf(src, target, 0);
    }

    private static int indexOf(byte[] src, byte[] target, int from) {

        for (int i = from; i <= src.length - target.length; i++) {
            boolean matched = true;

            for (int j = 0; j < target.length; j++) {
                if (src[i + j] != target[j]) {
                    matched = false;
                    break;
                }
            }

            if (matched) return i;
        }


        return -1;
    }


    /**
     * Content-Disposition 같은 헤더에서<br>
     * name="...", filename="..." 처럼 따옴표로 감싸진 값만 추출하는 함수<br>
     * <br>
     * multipart 헤더 예시<br>
     * Content-Disposition: form-data; name="file"; filename="test.png"<br>
     * <br>
     * 여기서<br>
     * extract(headers, "name");      // "file"<br>
     * extract(headers, "filename");  // "test.png"<br>
     *
     */

    private static String extract(String headers, String key) {
        String token = key + "=\"";
        int start = headers.indexOf(token);
        if (start == -1) {
            return null;
        }

        start += token.length();
        int end = headers.indexOf("\"", start);
        if (end == -1) return null;

        return headers.substring(start, end);
    }

    /**
     * Content-Type 헤더 추출<br>
     */
    private static String extractHeader(String headers, String name) {
        for (String line : headers.split("\r\n")) {
            if (line.toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT))) {
                int idx = line.indexOf(":");
                return line.substring(idx + 1).trim();
            }
        }
        return null;
    }

}
