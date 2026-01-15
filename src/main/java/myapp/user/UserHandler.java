package myapp.user;

import myapp.WebServer;
import myapp.bean.Component;
import myapp.file.UploadedFile;
import myapp.handler.HandlerMapping;
import myapp.http.*;
import myapp.webserver.HtmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Component
public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

    @HandlerMapping(method = HttpMethod.POST, path = "/user/create")
    public void register(HttpRequest request, HttpResponse response) {
        logger.debug("[register]");

        /*
         * request 처리 (POST: body)
         */
        Map<String, String> params = request.getBodyParams();

        String userId = params.get("userId");
        String password = params.get("password");
        String name = params.get("name");

        if (userId == null || userId.isBlank() || password == null || password.isBlank()
                || name == null || name.isBlank()) {
            throw new IllegalArgumentException("필수 회원가입 파라미터 누락");
        }

        User user = new User(userId, password, name);

        //  길이 검증 (최소 4글자)
        if (userId.length() < 4) {
            throw new IllegalArgumentException("아이디는 최소 4글자 이상이어야 합니다.");
        }

        if (name.length() < 4) {
            throw new IllegalArgumentException("닉네임은 최소 4글자 이상이어야 합니다.");
        }

        if (password.length() < 4) {
            throw new IllegalArgumentException("비밀번호는 최소 4글자 이상이어야 합니다.");
        }

        /*
         * 비즈니스 로직
         */
        UserDatabase.addUser(user);

        /*
         * redirect response
         */
        response.setStatus(302, "Found");
        response.addHeader(HttpHeader.LOCATION.value(), "/login");
    }


    @HandlerMapping(method = HttpMethod.POST, path = "/user/login")
    public void login(HttpRequest request, HttpResponse response) {

        Map<String, String> bodyParams = request.getBodyParams();
        String userId = bodyParams.get("userId");
        String password = bodyParams.get("password");

        User user = UserDatabase.findUserById(userId);

        // 아이디 없음 → 별도 안내 페이지
        if (user == null) {
            renderErrorPage(
                    response,
                    "존재하지 않는 아이디입니다.",
                    "/registration",
                    "회원 가입"
            );
            return;
        }

        // 비밀번호 틀림 → 로그인 페이지 유지
        if (!user.getPassword().equals(password)) {
            String html = HtmlLoader.loadStatic("/login/index.html");
            html = html.replace(
                    "{{ERROR_MESSAGE}}",
                    "비밀번호가 틀렸습니다."
            );
            html = html.replace("{{USER_ID}}", escapeHtml(userId));


            response.setStatus(200, "OK");
            response.addHeader(
                    HttpHeader.CONTENT_TYPE.value(),
                    "text/html; charset=UTF-8"
            );
            response.setBody(html.getBytes(StandardCharsets.UTF_8));
            return;
        }

        // 성공
        String sessionId = SessionManager.createSession(user);
        response.setStatus(302, "Found");
        response.addHeader(
                HttpHeader.SET_COOKIE.value(),
                "SID=" + sessionId + "; Path=/"
        );
        response.addHeader(
                HttpHeader.LOCATION.value(),
                "/index.html"
        );
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }


    private void renderErrorPage(
            HttpResponse response,
            String message,
            String buttonLink,
            String buttonText
    ) {
        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>로그인 실패</title>
                    <link href="/reset.css" rel="stylesheet"/>
                    <link href="/global.css" rel="stylesheet"/>
                </head>
                <body>
                    <div class="container">
                        <h2>%s</h2>
                
                        <div style="margin-top: 20px;">
                            <a class="btn btn_primary btn_size_m" href="%s">
                                %s
                            </a>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(message, buttonLink, buttonText);

        response.setStatus(200, "OK");
        response.addHeader(
                HttpHeader.CONTENT_TYPE.value(),
                "text/html; charset=UTF-8"
        );
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
    }


    @HandlerMapping(method = HttpMethod.POST, path = "/users/profile-image")
    public void uploadProfileImage(HttpRequest request, HttpResponse response) {

        /*
         * 1. request
         */

        // 1-1. 로그인 세션 확인
        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        if (user == null) {
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/login");
            return;
        }

        // 1-2. multipart 파일 파싱
        UploadedFile file = BodyParser.getMultipart(request);

        if (file == null) {
            response.badRequest();
            response.setBody("file not found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        /*
         * 2. 비즈니스 로직
         */

        try {
            // 2-1. 업로드 디렉토리 생성
            Path uploadDir = Paths.get("upload");
            Files.createDirectories(uploadDir);

            // 2-2. 파일 저장
            String storedName = UUID.randomUUID() + "_" + file.fileName();
            Path storedPath = uploadDir.resolve(storedName);

            Files.write(storedPath, file.data());

            // 2-3. (선택) 기존 프로필 이미지 삭제
            if (user.getProfileImage() != null) {
                String oldFileName =
                        Paths.get(user.getProfileImage()).getFileName().toString();
                Path oldPath = uploadDir.resolve(oldFileName);

                Files.deleteIfExists(oldPath);
            }

            // 2-4. 사용자 정보 갱신
            String imagePath = storedName;
            user.updateProfileImage(imagePath);

        } catch (IOException e) {
            logger.error("profile image upload failed", e);
            response.internalServerError();
            return;
        }

        /*
         * 3. response
         */

        response.ok();
    }

    @HandlerMapping(method = HttpMethod.DELETE, path = "/users/profile-image")
    public void deleteProfileImage(HttpRequest request, HttpResponse response) {

        /*
         * 1. request
         */

        // 1-1. 로그인 세션 확인
        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        if (user == null) {
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/login");
            return;
        }



        /*
         * 2. 비즈니스 로직
         */

        user.deleteProfileImage();

        /*
         * 3. response
         */

        response.ok();
    }

    @HandlerMapping(method = HttpMethod.GET, path = "/logout")
    public void logout(HttpRequest request, HttpResponse response) {

        // 1. 쿠키에서 SID 가져오기
        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);

        // 2. 세션 삭제
        SessionManager.expire(sessionId);

        /*
         * response
         * */

        response.setStatus(302, "Found");

        // 3. 쿠키 만료 (브라우저에서 제거)
        response.addHeader(
                HttpHeader.SET_COOKIE.value(),
                "SID=; Path=/; Max-Age=0"
        );

        // 4. 홈으로 리다이렉트
        response.addHeader(HttpHeader.LOCATION.value(), "/index.html");
    }

}
