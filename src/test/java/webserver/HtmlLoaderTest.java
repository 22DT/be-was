package webserver;

import dragon_tiger.webserver.HtmlLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlLoaderTest {


    @Test
    void load_test_html_success() {
        /*
         * given
         * */


        String path = "/test.html";


        /*
         * when
         * */

        String html = HtmlLoader.load(path);


        /*
         * then
         * */
        assertNotNull(html);
        assertTrue(html.contains("Hello"));
        assertTrue(html.contains("<html>"));

    }

    @Test
    void print_html() {
        String html = HtmlLoader.load("/test.html");

        System.out.println("===== HTML START =====");
        System.out.println(html);
        System.out.println("===== HTML END =====");
    }
}