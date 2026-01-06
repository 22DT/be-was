package webserver;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.UserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;
    private static final int THREAD_POOL_SIZE = 50;

    public static void main(String args[]) throws Exception {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }

        HandlerRegister handlerRegister = initHandlerMappings();

        ExecutorService executor =
                Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            while (true) {
                Socket connection = listenSocket.accept();

                executor.execute(new RequestHandler(connection, handlerRegister));
            }
        }
    }

    private static HandlerRegister initHandlerMappings() {
        HandlerRegister handlerRegister = new HandlerRegister();

        /*
        * 필요한 헨들러들 생성
        * */

        UserHandler userHandler = new UserHandler(); // 이거 DCL 도 고려해 보자
        PageHandler pageHandler = new PageHandler();

        /*
        * 등록
        * */

        // 회원가입
        handlerRegister.register(HttpMethod.POST, "/user/create", userHandler, "register", HttpRequest.class, HttpResponse.class);
        // 로그인
        handlerRegister.register(HttpMethod.POST, "/user/login", userHandler, "login", HttpRequest.class, HttpResponse.class);
        // 기본 페이지
        handlerRegister.register(HttpMethod.GET, "/index.html", pageHandler, "index", HttpRequest.class, HttpResponse.class);
        // 마이 페이지
        handlerRegister.register(HttpMethod.GET, "/mypage", pageHandler, "myPage", HttpRequest.class, HttpResponse.class);

        return handlerRegister;
    }
}
