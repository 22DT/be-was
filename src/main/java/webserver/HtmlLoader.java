package webserver;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HtmlLoader {
    public static String load(String path) {
        try (InputStream is = HtmlLoader.class.getResourceAsStream("/static" + path)) {

            if (is == null) {
                throw new IllegalStateException("HTML not found: " + path);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
