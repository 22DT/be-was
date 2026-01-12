package myapp.application;

import myapp.handler.HandlerDefinition;
import myapp.handler.HandlerRegister;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;

public class ApplicationDispatcher {
    private final HandlerRegister handlerRegister;
    private final StaticResourceHandler staticHandler;

    public ApplicationDispatcher(HandlerRegister handlerRegister,
                                 StaticResourceHandler staticHandler) {
        this.handlerRegister = handlerRegister;
        this.staticHandler = staticHandler;
    }

    public void dispatch(HttpRequest request, HttpResponse response) {
        HandlerDefinition handler = handlerRegister.findHandler(request);

        if (handler != null) {
            handler.handle(request, response);
        } else {
            staticHandler.handle(request, response);
        }
    }
}
