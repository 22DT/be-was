package webserver;

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

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Object handler() {
        return handler;
    }

    public Method target() {
        return target;
    }

    @Override
    public String toString() {
        return "HandlerDefinition{" +
                "method=" + method +
                ", path='" + path + '\'' +
                ", handler=" + handler.getClass().getSimpleName() +
                ", target=" + target.getName() +
                '}';
    }

}
