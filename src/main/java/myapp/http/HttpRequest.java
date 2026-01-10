package myapp.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> queryParams;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpRequest(HttpMethod method,
                       String path,
                       Map<String, String> queryParams,
                       String version,
                       Map<String, String> headers,
                       byte[] body) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return this.body;
    }

    public String getRequiredParam(String key) {
        String value = queryParams.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required query parameter: " + key);
        }
        return value;
    }

    public Map<String, String> getBodyParams() {
        String contentType = headers.get(HttpHeader.CONTENT_TYPE.lower());

        if (contentType == null ||
                !contentType.startsWith("application/x-www-form-urlencoded")) {
            return Map.of();
        }

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

    public String getCookie(String name) {
        String cookieHeader = headers.get(HttpHeader.COOKIE.lower());

        if (cookieHeader == null || cookieHeader.isBlank()) {
            return null;
        }

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] kv = cookie.trim().split("=", 2);

            if (kv.length == 2 && kv[0].equals(name)) {
                return kv[1];
            }
        }

        return null;
    }


}
