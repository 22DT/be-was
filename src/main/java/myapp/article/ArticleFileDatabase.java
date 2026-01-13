package myapp.article;

import java.util.ArrayList;
import java.util.List;

public class ArticleFileDatabase {
    private static final List<ArticleFile> store = new ArrayList<>();

    public static void save(ArticleFile articleFile) {
        store.add(articleFile);
    }

    public static List<Long> findFileIdsByArticleId(Long articleId) {
        return store.stream()
                .filter(af -> af.articleId().equals(articleId))
                .map(ArticleFile::fileId)
                .toList();
    }
}
