package webserver;

import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final String version;
    private final Map<String, String> headers;

    public HttpRequest(String method,
                       String path,
                       Map<String, String> queryParams,
                       String version,
                       Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.version = version;
        this.headers = headers;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getQueryParams(){return queryParams;}
    public String getVersion() { return version; }
    public Map<String, String> getHeaders() { return headers; }

}
