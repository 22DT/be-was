package webserver;

import application.ApplicationDispatcher;
import application.StaticResourceHandler;
import config.HandlerConfig;
import handler.HandlerRegister;
import network.BlockingConnection;
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


        /*
         * 애플리케이션 뜨기 위해 필요한 객체 들이 있네.. 이런 거 등록 흠..
         * */

        HandlerRegister handlerRegister = HandlerConfig.initializeWithAnnotations();
        StaticResourceHandler staticResourceHandler = new StaticResourceHandler();
        ApplicationDispatcher appDispatcher = new ApplicationDispatcher(handlerRegister, staticResourceHandler);

        ExecutorService executor =
                Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            while (true) {
                Socket socket = listenSocket.accept();

                BlockingConnection connection = new BlockingConnection(socket);

                executor.execute(new ConnectionHandler(connection, appDispatcher));
            }
        }
    }

}
