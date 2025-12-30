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
//            byte[] body = "<h1>Hello World</h1>".getBytes();

            byte[] body = new byte[0];

            /*
            * request
            *
            *
            *
            * 클라이언트가 HTTP 메시지를 보내면 소켓에 바이트스트림으로 저장되어 있음.
            * 왜 바이트 ? ->네 트워크는 0과 1만 앎. 다른 데이터 타입(문자열, int, 객체 등)은 상위 계층이 사용하는 타입임.
            * 해당 바이트스트림을 HTTP Request 객체로 변환
            * 왜? HTTP는 프로토콜임. 프로토콜은 형식과 의미를 갖음.
            * HTTP는 정해진 구조를 가지므로 이를 코드에서 다루기 위해 객채료 표현할 수 있음.
            * 또한, 의미는 필드명 or 타입으로 나타낼 수 있음.
            * */

            HttpRequest request = HttpRequestParser.parse(in);

            // HTTP Request 내용 출력
            logHttpRequest(request);

            /*
            * response
            *
            * 마찬가지로 HTTP 응답 또한 객채로 관리할 수 있음.
            * HTTP 응답을 만들고 소켓에 작성.
            * */

            HttpResponse response= handleRequest(request);

            HttpResponseWriter.write(dos, response);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
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
