package myapp.handler;

import myapp.http.HttpMethod;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;

public class TestArticleHandler {

    boolean called = false;
    String articleId;
    String commentId;

    @HandlerMapping(method = HttpMethod.GET, path = "/articles/{article-id}")
    public void getArticle(HttpRequest req, HttpResponse res) {
        called = true;
        articleId = req.getPathVariable("article-id");
    }

    @HandlerMapping(method = HttpMethod.GET, path = "/articles/{article-id}/comments/{comment-id}")
    public void getComment(HttpRequest req, HttpResponse res) {
        called = true;
        articleId = req.getPathVariable("article-id");
        commentId = req.getPathVariable("comment-id");
    }

    @HandlerMapping(method = HttpMethod.GET, path = "/articles/new")
    public void newArticle(HttpRequest req, HttpResponse res) {
        called = true;
    }
}
