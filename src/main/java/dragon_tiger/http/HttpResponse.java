package dragon_tiger.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private int statusCode;
    private String message;
    private final Map<String, String> headers;
    private byte[] body;


    public HttpResponse() {
        this.statusCode = 200;     // defaultthis.message = "OK";       // default
        this.headers = new HashMap<>();
    }

    public void setStatus(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(byte[] body) {
        this.body = body;
        if (body != null) {
            headers.put(HttpHeader.CONTENT_LENGTH.value(), String.valueOf(body.length));
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }


    public void ok() {
        setStatus(200, "OK");
    }

    public void badRequest() {
        setStatus(400, "Bad Request");
    }

    public void notFound() {
        setStatus(404, "Not Found");
    }

    public void internalServerError() {
        setStatus(500, "Internal Server Error");
    }
}



