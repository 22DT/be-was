package http;

import myapp.http.HttpResponse;
import myapp.http.HttpResponseEncoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpResponseEncoderTest {

    @Test
    void write_ok_response_with_body() throws Exception {

        /*
         * given
         * */

        HttpResponse response = new HttpResponse();
        response.ok();
        response.addHeader("Content-Type", "text/plain");
        response.setBody("hello".getBytes(StandardCharsets.UTF_8));

        FakeConnection connection = new FakeConnection();

        /*
         * when
         * */
        HttpResponseEncoder.write(connection, response);

        /*
         * then
         * */

        String actual = new String(
                connection.getWrittenBytes(),
                StandardCharsets.UTF_8
        );

        assertTrue(actual.startsWith("HTTP/1.1 200 OK\r\n"));

        assertTrue(actual.contains("Content-Type: text/plain\r\n"));
        assertTrue(actual.contains("Content-Length: 5\r\n"));

        assertTrue(actual.endsWith("\r\n\r\nhello"));
    }

    @Test
    void write_not_found_without_body() throws Exception {

        /*
         * given
         * */

        HttpResponse response = new HttpResponse();
        response.notFound();

        FakeConnection connection = new FakeConnection();

        /*
         * when
         * */
        HttpResponseEncoder.write(connection, response);

        /*
         * then
         * */

        String actual = new String(
                connection.getWrittenBytes(),
                StandardCharsets.UTF_8
        );

        assertEquals(
                "HTTP/1.1 404 Not Found\r\n\r\n",
                actual
        );
    }


}