package webserver;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    OPTIONS,
    HEAD;

    public static HttpMethod of(String raw) {
        if (raw == null) return null;

        try {
            return HttpMethod.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

