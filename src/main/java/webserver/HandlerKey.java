package webserver;

import java.util.Objects;

public final class HandlerKey {

    private final HttpMethod method;
    private final String path;

    public HandlerKey(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandlerKey)) return false;
        HandlerKey that = (HandlerKey) o;
        return method == that.method &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }

}
