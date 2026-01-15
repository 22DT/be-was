package myapp.application;

import myapp.article.Article;
import myapp.article.ArticleDatabase;
import myapp.bean.Component;
import myapp.handler.HandlerMapping;
import myapp.http.HttpHeader;
import myapp.http.HttpMethod;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;
import myapp.user.SessionManager;
import myapp.user.User;
import myapp.webserver.HtmlLoader;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class PageHandler {
    @HandlerMapping(method = HttpMethod.GET, path = "/index.html")
    public void index(HttpRequest request, HttpResponse response) {

        /*
         * 1. 로그인 여부 판단
         */
        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        /*
         * 2. 최신 글 확인 → 있으면 redirect
         */
        Article latest = ArticleDatabase.findLatest();
        if (latest != null) {
            response.setStatus(302, "Found");
            response.addHeader("Location", "/articles/" + latest.articleId());
            return;
        }

        /*
         * 3. index.html 읽기 (최신 글 없을 때만)
         */
        String html = HtmlLoader.loadStatic("/index.html");

        /*
         * 4. HEADER_MENU 가공
         */
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
                        <a href="/mypage" class="header__greeting-link">
                            안녕하세요, %s님!
                        </a>
                    </li>
                    <li class="header__menu__item">
                        <a class="btn btn_contained btn_size_s" href="/article/write.html">
                            글쓰기
                        </a>
                    </li>
                    <li class="header__menu__item">
                        <a class="btn btn_ghost btn_size_s" href="/logout">
                            로그아웃
                        </a>
                    </li>
                    """.formatted(user.getUserId()));

        }

        html = html.replace("{{HEADER_MENU}}", headerMenu.toString());

        /*
         * 5. ARTICLE_CONTENT (empty state)
         */
        String articleContent = """
                    <div class="post__empty" style="text-align:center; padding:60px;">
                        <p>아직 작성된 게시글이 없습니다.</p>
                        <a href="/article/write.html"
                           class="btn btn_primary btn_size_m"
                           style="margin-top:20px; display:inline-block;">
                            첫 글 작성하기
                        </a>
                    </div>
                """;

        html = html.replace("{{ARTICLE_CONTENT}}", articleContent);

        /*
         * 6. response
         */
        response.setStatus(200, "OK");
        response.addHeader(HttpHeader.CONTENT_TYPE.value(), "text/html; charset=UTF-8");
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
    }


    @HandlerMapping(method = HttpMethod.GET, path = "/mypage")
    public void myPage(HttpRequest request, HttpResponse response) {

        /*
         * 1. 세션 확인
         */
        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        /*
         * 2. 로그인 안 되어 있으면 redirect
         */
        if (user == null) {
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/login");
            return;
        }

        /*
         * 3. html 로드
         */
        String html = HtmlLoader.loadStatic("/mypage/index.html");

        /*
         * 4. HEADER_MENU 생성 (중요!)
         */
        StringBuilder headerMenu = new StringBuilder();

        headerMenu.append("""
                <li class="header__menu__item">
                    <a href="/mypage" class="header__greeting-link">
                        안녕하세요, %s님!
                    </a>
                </li>
                <li class="header__menu__item">
                    <a class="btn btn_contained btn_size_s" href="/article/write.html">
                        글쓰기
                    </a>
                </li>
                <li class="header__menu__item">
                    <a class="btn btn_ghost btn_size_s" href="/logout">
                        로그아웃
                    </a>
                </li>
                """.formatted(user.getUserId()));

        html = html.replace("{{HEADER_MENU}}", headerMenu.toString());

        /*
         * 5. html 가공
         */
        // 닉네임
        html = html.replace("{{USER_NAME}}", user.getName());

        // 프로필 이미지
        String profileImageUrl;
        if (user.getProfileImage() != null) {
            profileImageUrl = "/files/" + URLEncoder.encode(
                    user.getProfileImage(),
                    StandardCharsets.UTF_8
            );
        } else {
            profileImageUrl = "/img/basic_profileImage.svg";
        }

        html = html.replace("{{PROFILE_IMAGE_URL}}", profileImageUrl);

        /*
         * 6. response
         */
        response.ok();
        response.addHeader(
                HttpHeader.CONTENT_TYPE.value(),
                "text/html; charset=UTF-8"
        );
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
    }


    @HandlerMapping(method = HttpMethod.GET, path = "/article/write.html")
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

    @HandlerMapping(method = HttpMethod.GET, path = "/login")
    public void loginPage(HttpRequest request, HttpResponse response) {

        // 1. 이미 로그인 상태면 메인으로 보내도 됨 (선택)
        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        if (user != null) {
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/index.html");
            return;
        }

        // 2. login.html 로드
        String html = HtmlLoader.loadStatic("/login/index.html");

        // 3. 템플릿 치환 (중요!!)
        html = html.replace("{{ERROR_MESSAGE}}", "");
        html = html.replace("{{USER_ID}}", "");

        // 4. response
        response.setStatus(200, "OK");
        response.addHeader(
                HttpHeader.CONTENT_TYPE.value(),
                "text/html; charset=UTF-8"
        );
        response.setBody(html.getBytes(StandardCharsets.UTF_8));
    }

}
