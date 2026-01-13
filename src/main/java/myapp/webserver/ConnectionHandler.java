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
            while (true) {
                int read = connection.read(readBuffer);

                if (read == -1) {
                    connection.close();
                    return;
                }

                // write → read
                readBuffer.flip();

                HttpRequest request = HttpRequestParser.parseHeader(readBuffer);

                // 아직 요청이 완성되지 않음
                if (request == null) {
                    readBuffer.compact();
                    continue;
                }

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
                    body = connection.readExactly(readBuffer, contentLength);
                }

                request.setBody(body);

                // 🔥 body까지 다 소비했으므로 다음 read 준비
                readBuffer.compact();

                // HTTP Request 로그
                logHttpRequest(request);

                HttpResponse response = new HttpResponse();
                appDispatcher.dispatch(request, response);

                // 응답 쓰기
                connection.write(HttpResponseEncoder.encodeHeaders(response));

                ByteBuffer bodyBuf = HttpResponseEncoder.encodeBody(response);
                if (bodyBuf != null) {
                    connection.write(bodyBuf);
                }

                // keep-alive 미지원
                connection.close();
                return;
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            try {
                connection.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
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
