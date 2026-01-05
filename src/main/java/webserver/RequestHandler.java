package webserver;

import http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final HandlerRegister handlerRegister;

    private static final String STATIC_DIR = "src/main/resources/static";

    public RequestHandler(Socket connectionSocket, HandlerRegister handlerRegister) {
        this.connection = connectionSocket;
        this.handlerRegister = handlerRegister;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);

            /*
             * request response 준비
             * */

            HttpRequest request = HttpRequestParser.parse(in);
            HttpResponse response = new HttpResponse();

            // HTTP Request 내용 출력
            logHttpRequest(request);


            /*
             * request 처리
             * */

            handleRequest(request, response);

            /*
             * response
             * */

            HttpResponseWriter.write(dos, response);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void handleRequest(HttpRequest request, HttpResponse response) {
        try {

            // 라우팅

            HandlerDefinition handler =
                    handlerRegister.get(request.getMethod(), request.getPath());
            // 1. 동적 리소스
            if (handler != null) {
                handler.handle(request, response);
            }
            // 2. 정적 리소스
            else {
                handleStatic(request, response);
            }

        } catch (Exception e) {

            // 동적도 없고 정적도 없음 → 404
            logger.debug("Request not handled. method={}, path={}",
                    request.getMethod(), request.getPath(), e);

            response.setStatus(404, "Not Found");
            response.setBody("Not Found".getBytes());
        }
    }


    private void handleStatic(HttpRequest request, HttpResponse response) {
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
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith("ico")) return "image/x-icon";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private void logHttpRequest(HttpRequest request) {

        logger.debug("===== HTTP REQUEST =====");
        logger.debug("Method  : {}", request.getMethod());
        logger.debug("Path    : {}", request.getPath());
        logger.debug("Version : {}", request.getVersion());

        logger.debug("----- Query Params -----");
        if (request.getQueryParams().isEmpty()) {
            logger.debug("(none)");
        } else {
            request.getQueryParams().forEach((key, value) ->
                    logger.debug("{} = {}", key, value)
            );
        }

        logger.debug("----- Headers -----");
        request.getHeaders().forEach((key, value) ->
                logger.debug("{}: {}", key, value)
        );

        logger.debug("----- Body -----");
        byte[] body = request.getBody();
        if (body == null || body.length == 0) {
            logger.debug("(none)");
        } else {
            String bodyString = new String(body, StandardCharsets.UTF_8);
            logger.debug(bodyString);
        }

        logger.debug("========================");
    }

}
