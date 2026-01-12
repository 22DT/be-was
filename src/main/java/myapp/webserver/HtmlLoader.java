package myapp.webserver;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HtmlLoader {

    // ===== static 리소스 =====
    public static String loadStatic(String path) {
        return loadFrom("/static", path);
    }

    // ===== template 리소스 =====
    public static String loadTemplate(String path) {
        return loadFrom("/templates", path);
    }

    // ===== 공통 로직 =====
    private static String loadFrom(String basePath, String path) {
        try (InputStream is =
                     HtmlLoader.class.getResourceAsStream(basePath + path)) {

            if (is == null) {
                throw new IllegalStateException("HTML not found: " + basePath + path);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
