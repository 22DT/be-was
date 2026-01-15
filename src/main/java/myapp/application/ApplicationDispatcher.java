package myapp.application;

import myapp.handler.HandlerDefinition;
import myapp.handler.HandlerRegister;
import myapp.http.HttpHeader;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ApplicationDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDispatcher.class);
    private final HandlerRegister handlerRegister;
    private final StaticResourceHandler staticHandler;

    public ApplicationDispatcher(HandlerRegister handlerRegister,
                                 StaticResourceHandler staticHandler) {
        this.handlerRegister = handlerRegister;
        this.staticHandler = staticHandler;
    }

    public void dispatch(HttpRequest request, HttpResponse response) {
        HandlerDefinition handler = handlerRegister.findHandler(request);

        try {
            if (handler != null) {
                handler.handle(request, response);
            } else {
                staticHandler.handle(request, response);
            }
        } catch (Exception e) {
            logger.error(
                    "[dispatch][error] handler={}",
                    handler != null ? handler.getClass() : "static",
                    e
            );
            response.setStatus(400, "Internal Server Error");
            response.addHeader(HttpHeader.CONTENT_TYPE.value(), "text/plain; charset=UTF-8");
            response.setBody(e.getMessage().getBytes(StandardCharsets.UTF_8));
        }


    }
}
