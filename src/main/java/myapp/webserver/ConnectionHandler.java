package myapp.webserver;

import myapp.application.ApplicationDispatcher;
import myapp.http.*;
import myapp.network.BlockingConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    private final BlockingConnection connection;
    private final ApplicationDispatcher appDispatcher;

    public ConnectionHandler(BlockingConnection connection, ApplicationDispatcher appDispatcher) {
        this.connection = connection;
        this.appDispatcher = appDispatcher;
    }

    public void run() {

        Socket socket = connection.getSocket();
        logger.debug("New Client Connect! Connected IP : {}, Port : {}",
                socket.getInetAddress(), socket.getPort());

        ByteBuffer readBuffer = ByteBuffer.allocate(8192);

        try {
            // 1. 헤더 읽기 (스트리밍)
            HttpRequest request = readRequest(readBuffer);
            if (request == null) {
                connection.close();
                return;
            }

            // 2. Body 읽기 (Content-Length 기반)
            readRequestBody(request, readBuffer);

            // 3. 로그
            logHttpRequest(request);

            // 4. 디스패치
            HttpResponse response = new HttpResponse();
            appDispatcher.dispatch(request, response);

            // 5. 응답
            connection.write(HttpResponseEncoder.encodeHeaders(response));
            ByteBuffer bodyBuf = HttpResponseEncoder.encodeBody(response);
            if (bodyBuf != null) {
                connection.write(bodyBuf);
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            // keep-alive 미지원
            try {
                connection.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * HTTP 헤더가 완성될 때까지 반복해서 읽는다
     */
    private HttpRequest readRequest(ByteBuffer buffer) throws IOException {
        while (true) {
            int read = connection.read(buffer);
            if (read == -1) {
                return null;
            }

            buffer.flip();

            HttpRequest request = HttpRequestParser.parseHeader(buffer);
            if (request == null) {
                buffer.compact();
                continue;
            }

            return request;
        }
    }

    /**
     * Content-Length가 있는 경우 body를 정확히 읽는다
     */
    private void readRequestBody(HttpRequest request,
                                 ByteBuffer buffer) throws IOException {

        String cl = request.getHeader(HttpHeader.CONTENT_LENGTH);
        int contentLength = 0;

        if (cl != null) {
            contentLength = Integer.parseInt(cl);
            if (contentLength < 0) {
                throw new IllegalArgumentException("Invalid Content-Length");
            }
        }

        byte[] body = null;
        if (contentLength > 0) {
            body = connection.readExactly(buffer, contentLength);
        }

        request.setBody(body);

        // body까지 소비했으므로 다음 read 준비
        buffer.compact();
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
