package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        String method = parts[0];
        String url = parts[1];
        String version = parts[2];


        // 2. url 에서 path / query 분리
        String[] urlParts = url.split("\\?");
        String path = urlParts[0];

        Map<String, String> queryParams = new HashMap<>();

        if(urlParts.length==2){
            String queryString=urlParts[1];

            String[] pairs=queryString.split("&");
            for(String pair:pairs){
                String[] kv=pair.split("=", 2);
                String key=kv[0];
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

        return new HttpRequest(method, path, queryParams, version, headers);
    }
}
