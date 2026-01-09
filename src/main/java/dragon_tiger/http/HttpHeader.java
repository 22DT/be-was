package dragon_tiger.http;

public enum HttpHeader {
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type"),
    LOCATION("Location"),
    COOKIE("Cookie"),
    SET_COOKIE("Set-Cookie"),
    SESSION_COOKIE_NAME("SID");


    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

