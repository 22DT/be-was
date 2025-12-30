package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * HTTP는 프로토콜임.
 * 프토로콜이란? 형식과 의미를 갖는다.
 * HTTP는 정해진 구조를 가지므로 이를 코드에서 다루기 위해 객채료 표현할 수 있음.
 */

public class HttpRequest {

    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;

    public HttpRequest(String method, String path, String version,
                       Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getVersion() { return version; }
    public Map<String, String> getHeaders() { return headers; }


}
