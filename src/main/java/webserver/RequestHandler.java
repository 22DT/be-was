package webserver;

import application.ApplicationDispatcher;
import http.HttpRequest;
import http.HttpRequestParser;
import http.HttpResponse;
import http.HttpResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final ApplicationDispatcher appDispatcher;

    private static final String STATIC_DIR = "src/main/resources/static";

    public RequestHandler(Socket connectionSocket, ApplicationDispatcher appDispatcher) {
        this.connection = connectionSocket;
        this.appDispatcher = appDispatcher;
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
             * dispatcher 로 넘겨준다.
             * */

            appDispatcher.dispatch(request, response);

            /*
             * response
             * */

            HttpResponseWriter.write(dos, response);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
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
