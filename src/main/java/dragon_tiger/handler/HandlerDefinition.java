package dragon_tiger.handler;

import dragon_tiger.http.HttpMethod;
import dragon_tiger.http.HttpRequest;
import dragon_tiger.http.HttpResponse;

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

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Object getHandler() {
        return handler;
    }

    public Method getTarget() {
        return target;
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
