package myapp.article;

import myapp.file.FileDataBase;
import myapp.file.FileInfo;
import myapp.handler.HandlerMapping;
import myapp.http.HttpHeader;
import myapp.http.HttpMethod;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;
import myapp.user.SessionManager;
import myapp.user.User;
import myapp.webserver.HtmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class ArticleHandler {
    private static final Logger logger = LoggerFactory.getLogger(ArticleHandler.class);

    /*
     * c
     * */


    @HandlerMapping(method = HttpMethod.POST, path = "/articles")
    public void create(HttpRequest request, HttpResponse response) {

        /*
         * request
         * */

        // 로그인 여부 확인

        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        // 로그인 페이지로 redirect
        if (user == null) {
            logger.warn("[create][로그인 안 했음]");
            response.setStatus(302, "Found");
            response.addHeader(HttpHeader.LOCATION.value(), "/login");

            return;
        }

        Map<String, String> params = request.getBodyParams();

        String content = params.get("content");

        if (content == null || content.isBlank()) {
            logger.warn("[create][content null or blank]");
        }

        String imagePath = params.get("imagePath");



        /*
         * 비즈니스 로직
         * */


        // 게시글 생성
        Article article = ArticleDatabase.addArticle(user.getUserId(), content);

        // 파일 갖고 온다
        if (imagePath != null && !imagePath.isBlank()) {
            FileInfo file = FileDataBase.findByPath(imagePath);

            Long fileId = file.fileId();

            ArticleFile articleFile = new ArticleFile(article.articleId(), fileId);

            ArticleFileDatabase.save(articleFile);
        }

        /*
         * response
         * */

        // 해당 글로 redirect
        response.setStatus(302, "Found");
        response.addHeader(HttpHeader.LOCATION.value(), "/articles/" + article.articleId());
    }


    /*
     * r
     * */

    @HandlerMapping(method = HttpMethod.GET, path = "/articles/{article-id}")
    public void getArticle(HttpRequest request, HttpResponse response) {
        /*
         * response
         * */

        String articleId = request.getPathVariable("article-id");


        /*
         * 비즈니스 로직
         * */

        // 게시글 찾아온다.
        Article article = ArticleDatabase.findArticleById(Long.valueOf(articleId));

        if (article == null) {
            response.setStatus(404, "Not Found");
            response.setBody("Article Not Found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        // 파일

        List<Long> fileIds =
                ArticleFileDatabase.findFileIdsByArticleId(article.articleId());

        String imageHtml = "";

        if (!fileIds.isEmpty()) {
            Long fileId = fileIds.get(0); //  첫 번째 파일만 사용
            FileInfo file = FileDataBase.findById(fileId);

            if (file != null) {
                imageHtml = """
                            <img class="post__img" src="/files/%s"/>
                        """.formatted(file.storedName());

            }
        }


        // html 처리
        String html = HtmlLoader.loadTemplate("/article/detail.html");

        String sessionId = request.getCookie(SessionManager.SESSION_COOKIE_NAME);
        User user = SessionManager.getLoginUser(sessionId);

        StringBuilder headerMenu = new StringBuilder();

        // 로그인 여부 판단
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

        // html 치환
        html = html.replace("{{HEADER_MENU}}", headerMenu.toString());

        String content = escapeHtml(article.content())
                .replace("\n", "<br>");

        html = html.replace("{{content}}", content);

        html = html.replace("{{ARTICLE_IMAGE}}", imageHtml);

        /*
         * response
         * */

        response.setStatus(200, "OK");
        response.addHeader(HttpHeader.CONTENT_TYPE.value(), "text/html; charset=UTF-8");
        response.setBody(html.getBytes(StandardCharsets.UTF_8));

    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }


}
