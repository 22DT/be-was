package dragon_tiger.application;

import dragon_tiger.handler.HandlerDefinition;
import dragon_tiger.handler.HandlerRegister;
import dragon_tiger.http.HttpRequest;
import dragon_tiger.http.HttpResponse;

public class ApplicationDispatcher {
    private final HandlerRegister handlerRegister;
    private final StaticResourceHandler staticHandler;

    public ApplicationDispatcher(HandlerRegister handlerRegister,
                                 StaticResourceHandler staticHandler) {
        this.handlerRegister = handlerRegister;
        this.staticHandler = staticHandler;
    }

    public void dispatch(HttpRequest request, HttpResponse response) {
        HandlerDefinition handler = handlerRegister.get(request.getMethod(), request.getPath());

        if (handler != null) {
            handler.handle(request, response);
        } else {
            staticHandler.handle(request, response);
        }
    }
}
