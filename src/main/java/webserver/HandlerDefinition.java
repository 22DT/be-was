package webserver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandlerDefinition {

    private final HttpMethod method;
    private final String path;
    private final Object handler;
    private final Method target;

    public HandlerDefinition(HttpMethod method, String path, Object handler, Method target) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.target = target;
    }

    public void handle(HttpRequest request, HttpResponse response) {
        try {
            target.invoke(handler, request, response);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Cannot access handler method: " + target.getName(), e
            );
        } catch (InvocationTargetException e) {
            // 실제 handler 내부에서 던진 예외
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
    }

}
