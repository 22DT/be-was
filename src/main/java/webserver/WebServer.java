package webserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.User;
import model.UserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        /*
         * 핸들러 수동 등록
         * */

        HandlerRegister handlerRegister = new HandlerRegister();

        UserHandler userHandler = new UserHandler(); // 이거 DCL 도 고려해 보자

        // 회원가입
        handlerRegister.register(HttpMethod.GET, "/create", userHandler, "register", User.class);
        // 로그인
        handlerRegister.register(HttpMethod.POST, "/login", userHandler, "login", String.class, String.class);

        ExecutorService executor =
                Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            while (true) {
                Socket connection = listenSocket.accept();

                executor.execute(new RequestHandler(connection));
            }
        }
    }
}
