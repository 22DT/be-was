package myapp.application;

import myapp.bean.Component;
import myapp.handler.HandlerMapping;
import myapp.http.HttpHeader;
import myapp.http.HttpMethod;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;
import myapp.user.SessionManager;
import myapp.user.User;
import myapp.webserver.HtmlLoader;

import java.nio.charset.StandardCharsets;

@Component
public class PageHandler {
    @HandlerMapping(method = HttpMethod.GET, path = "/index.html")
    public void index(HttpRequest request, HttpResponse response) {
        /*
         * 1. 로그인 여부 판단
         * */

        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);


        /*
         * 2. index.html 읽기
         * */

        String html = HtmlLoader.loadStatic("/index.html");


        /*
         * 3. html 가공
         * */

        StringBuilder headerMenu = new StringBuilder();

        if (user == null) {
            headerMenu.append("""
                    <li class="header__menu__item">
                      <a class="btn btn_contained btn_size_s" href="/login">로그인</a>
                    </li>
                    <li class="header__menu__item">
                      <a class="btn btn_ghost btn_size_s" href="/registration">회원 가입</a>
                    </li>
                    """);
        } else {
            headerMenu.append("""
                    <li class="header__menu__item">
                      <a class="btn btn_contained btn_size_s" href="/mypage">
                    """);
            headerMenu.append(user.getUserId());
            headerMenu.append("""
                      </a>
                    </li>
                    <li class="header__menu__item">
                      <a class="btn btn_ghost btn_size_s" href="/logout">로그아웃</a>
                    </li>
                    """);
        }

        html = html.replace("{{HEADER_MENU}}", headerMenu.toString());



        /*
         * 4. response
         * */

        response.setStatus(200, "OK");
        response.addHeader(HttpHeader.CONTENT_TYPE.value(), "text/html; charset=UTF-8");
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
    }

    @HandlerMapping(method = HttpMethod.GET, path = "/mypage")
    public void myPage(HttpRequest request, HttpResponse response) {
        /*
         * 1. 세션 확인
         * */
        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        /*
         * 2. 로그인 안 되어 있으면 redirect
         * */
        if (user == null) {
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/login");

            return;
        }

        /*
         * 3. mypage.html 읽기
         * */

        String html = HtmlLoader.loadStatic("/mypage/index.html");

        /*
         * 4. 사용자 정보로 html 가공
         * */

        html = html.replace("{{USER_NAME}}", user.getName());

        /*
         * 5. 응답
         * */

        response.setStatus(200, "OK");
        response.addHeader(HttpHeader.CONTENT_TYPE.value(), "text/html; charset=UTF-8");
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
    }


    @HandlerMapping(method = HttpMethod.GET, path = "/article/index.html")
    public void redirectToWriteOrLogin(HttpRequest request, HttpResponse response) {
        /*
         * 로그인 여부 확인
         * */

        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        // 로그인 페이지로 redirect
        if (user == null) {
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/login");

            return;
        }


        String path = request.getPath();

        String html = HtmlLoader.loadStatic(path);

        response.setStatus(200, "OK");
        response.addHeader(HttpHeader.CONTENT_TYPE.value(), "text/html; charset=UTF-8");
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
    }
}
