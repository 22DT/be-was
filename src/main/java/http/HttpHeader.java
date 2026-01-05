package http;

public enum HttpHeader {
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type")




    ;


    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

