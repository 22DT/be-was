package webserver;

import http.HttpRequest;
import http.HttpRequestParser;
import model.User;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    @Test
    void bodyParams를_User객체로_변환한다() throws IOException {
        /*
        * given
        * */

        String body = "userId=kim&password=1234&name=김철수&email=kim@test.com";

        String request =
                "POST /register HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Content-Type: application/x-www-form-urlencoded\r\n" +
                        "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                        "\r\n" +
                        body;

        /*
        * when
        * */

        HttpRequest httpRequest = HttpRequestParser.parse(new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)));

        Map<String, String> params = httpRequest.getBodyParams();

        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));


        /*
         * then
         */

        assertEquals("kim", user.getUserId());
        assertEquals("1234", user.getPassword());
        assertEquals("김철수", user.getName());
        assertEquals("kim@test.com", user.getEmail());

    }

}