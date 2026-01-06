package handler;

import http.HttpHeader;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.SessionManager;
import model.User;
import webserver.HtmlLoader;

import java.nio.charset.StandardCharsets;

public class PageHandler {
    @HandlerMapping(method= HttpMethod.GET, path="/index.html")
    public void index(HttpRequest request, HttpResponse response) {
        /*
         * 1. 로그인 여부 판단
         * */

        String sessionId = request.getCookie(HttpHeader.SESSION_COOKIE_NAME.value());
        User user = SessionManager.getLoginUser(sessionId);


        /*
         * 2. index.html 읽기
         * */

        String html = HtmlLoader.load("/index.html");


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

    @HandlerMapping(method = HttpMethod.GET, path="/mypage")
    public void myPage(HttpRequest request, HttpResponse response){
        /*
        * 1. 세션 확인
        * */
        String sessionId = request.getCookie(HttpHeader.SESSION_COOKIE_NAME.value());
        User user = SessionManager.getLoginUser(sessionId);

        /*
        * 2. 로그인 안 되어 있으면 redirect
        * */
        if(user==null){
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/login");

            return;
        }

        /*
        * 3. mypage.html 읽기
        * */

        String html = HtmlLoader.load("/mypage/index.html");

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
}
