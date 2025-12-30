package webserver;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestParserTest {

    @Test
    void query_string_없음() throws IOException {
        /*
        * given
        * */
        String request =
                "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";

        /*
        * when
        * */

        HttpRequest httpRequest =
                HttpRequestParser.parse(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));

        /*
        * then
        * */

        assertEquals("/index.html", httpRequest.getPath());
        assertTrue(httpRequest.getQueryParams().isEmpty());
    }

    @Test
    void query_string_있음() throws IOException {
        /*
         * given
         * */

        String request =
                "GET /create?userId=user&password=pass HTTP/1.1\r\n" +
                        "Hos: localHost:8080\r\n" +
                        "\r\n";

        /*
         * when
         * */

        HttpRequest httpRequest =
                HttpRequestParser.parse(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));

        /*
         * then
         */
        assertEquals("/create", httpRequest.getPath());
        assertEquals(2, httpRequest.getQueryParams().size());

        assertEquals("user", httpRequest.getQueryParams().get("userId"));
        assertEquals("pass", httpRequest.getQueryParams().get("password"));

    }

    @Test
    void 값이_없는_query_parameter() throws Exception {
        /*
        * given
        * */
        String request =
                "GET /search?keyword HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "\r\n";

        /*
        * when
        * */
        HttpRequest httpRequest = HttpRequestParser.parse(
                new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8))
        );

        /*
        * then
        * */
        assertEquals(1, httpRequest.getQueryParams().size());
        assertEquals("", httpRequest.getQueryParams().get("keyword"));
    }

}