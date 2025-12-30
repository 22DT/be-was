package webserver;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private int statusCode;
    private String message;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public static HttpResponse ok() {
        HttpResponse res = new HttpResponse();
        res.statusCode = 200;
        res.message = "OK";
        return res;
    }

    public static HttpResponse notFound() {
        HttpResponse res = new HttpResponse();
        res.statusCode = 404;
        res.message = "Not Found";
        return res;
    }

    public static HttpResponse internalServerError() {
        HttpResponse res = new HttpResponse();
        res.statusCode = 500;
        res.message = "Internal Server Error";
        return res;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(byte[] body) {
        this.body = body;
        if (body != null) {
            headers.put("Content-Length", String.valueOf(body.length));
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
}


