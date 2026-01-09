package dragon_tiger.application;

import dragon_tiger.bean.Component;
import dragon_tiger.http.HttpHeader;
import dragon_tiger.http.HttpRequest;
import dragon_tiger.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component("staticHandler")
public class StaticResourceHandler {
    private static final String STATIC_DIR = "src/main/resources/static";


    public void handle(HttpRequest request, HttpResponse response) {
        String path = request.getPath();

        try {
            File file = new File(STATIC_DIR + path);

            if (file.isDirectory()) {
                file = new File(file, "index.html");
                path = path + "/index.html";
            }

            if (!file.exists()) {
                throw new RuntimeException("Static resource not found: " + path);
            }

            byte[] body = Files.readAllBytes(file.toPath());

            response.setStatus(200, "OK");
            response.addHeader(HttpHeader.CONTENT_TYPE.value(), resolveContentType(path));
            response.setBody(body);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String resolveContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "dragon_tiger/application/javascript; charset=UTF-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith("ico")) return "image/x-icon";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "dragon_tiger/application/octet-stream";
    }
}
