package myapp.article;


import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ArticleDatabase {
    private static final Map<Long, Article> Articles = new ConcurrentHashMap<>();
    private static final AtomicLong ARTICLE_ID_SEQUENCE = new AtomicLong(1);

    public static Long nextArticleId() {
        return ARTICLE_ID_SEQUENCE.getAndIncrement();
    }

    public static Article addArticle(String writerId, String content) {
        Long id = nextArticleId();
        Article article = new Article(id, content, writerId);

        Articles.putIfAbsent(id, article);

        return article;
    }

    public static Article findArticleById(Long ArticleId) {
        return Articles.get(ArticleId);
    }

    public static Collection<Article> findAll() {
        return Articles.values();
    }
}
