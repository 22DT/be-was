package myapp.user;

import myapp.WebServer;
import myapp.bean.Component;
import myapp.file.UploadedFile;
import myapp.handler.HandlerMapping;
import myapp.http.*;
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
        String email = params.get("email");

        if (userId == null || userId.isBlank() || password == null || password.isBlank()
                || name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("필수 회원가입 파라미터 누락");
        }

        User user = new User(userId, password, name, email);

        /*
         * 비즈니스 로직
         */
        User prev = UserDatabase.addUser(user);
        if (prev != null) {
            throw new IllegalArgumentException(
                    "이미 존재하는 userId: " + user.getUserId()
            );
        }

        /*
         * redirect response
         */
        response.setStatus(302, "Found");
        response.addHeader(HttpHeader.LOCATION.value(), "/index.html");
    }


    @HandlerMapping(method = HttpMethod.POST, path = "/user/login")
    public void login(HttpRequest request, HttpResponse response) {

        /*
         * request
         * */

        Map<String, String> bodyParams = request.getBodyParams();

        String userId = bodyParams.get("userId");
        String password = bodyParams.get("password");

        if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("필수 회원가입 파라미터 누락");
        }


        /*
         * 비즈니스 로직
         * */

        User user = UserDatabase.findUserById(userId);

        // 1. 아이디 없음
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        // 2. 비밀번호 불일치
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 로그인 성공

        // 세션 생성
        String sessionId = SessionManager.createSession(user);

        /*
         * response
         * */

        response.setStatus(302, "Found");
        response.addHeader(HttpHeader.SET_COOKIE.value(), "SID=" + sessionId + "; Path=/");
        response.addHeader(HttpHeader.LOCATION.value(), "/index.html");
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
}
