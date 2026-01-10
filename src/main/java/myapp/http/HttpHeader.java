package myapp.http;

import java.util.Locale;

public enum HttpHeader {
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type"),
    LOCATION("Location"),
    COOKIE("Cookie"),
    SET_COOKIE("Set-Cookie");


    private final String value;
    private final String lowerCase;

    HttpHeader(String value) {
        this.value = value;
        this.lowerCase = value.toLowerCase(Locale.ROOT);
    }

    public String value() {
        return value;
    }

    public String lower() {
        return lowerCase;
    }
}

