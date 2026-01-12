package myapp.user;

import myapp.WebServer;
import myapp.bean.Component;
import myapp.db.UserDatabase;
import myapp.handler.HandlerMapping;
import myapp.http.HttpHeader;
import myapp.http.HttpMethod;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
}
