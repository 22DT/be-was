package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private static final String STATIC_DIR = "src/main/resources/static";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);

            /*
             * request
             * */

            HttpRequest request = HttpRequestParser.parse(in);

            // HTTP Request 내용 출력
            logHttpRequest(request);

            /*
             * 비즈니스 로직
             * */

            HttpResponse response = handleRequest(request);

            /*
             * response
             * */

            HttpResponseWriter.write(dos, response);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private HttpResponse handleRequest(HttpRequest request) {
        String path = request.getPath();

        try {

            File file = new File(STATIC_DIR + path);

            if (!file.exists() || !file.isFile()) {
                return HttpResponse.notFound();
            }

            byte[] body = Files.readAllBytes(file.toPath());

            HttpResponse response = HttpResponse.ok();
            response.addHeader("Content-Type", resolveContentType(path));
            response.setBody(body);

            return response;

        } catch (IOException e) {
            return HttpResponse.internalServerError();
        }
    }

    private String resolveContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        if(path.endsWith("ico")) return "image/x-icon";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private void logHttpRequest(HttpRequest request) {

        logger.debug("===== HTTP REQUEST =====");
        logger.debug("Method  : {}", request.getMethod());
        logger.debug("Path    : {}", request.getPath());
        logger.debug("Version : {}", request.getVersion());

        logger.debug("----- Headers -----");
        request.getHeaders().forEach((key, value) ->
                logger.debug("{}: {}", key, value)
        );

        logger.debug("========================");
    }

}
