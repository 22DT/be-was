package myapp.config;

import myapp.application.PageHandler;
import myapp.article.ArticleHandler;
import myapp.file.FileHandler;
import myapp.handler.HandlerRegister;
import myapp.user.UserHandler;

public class HandlerConfig {
    public static HandlerRegister initialize() {
        HandlerRegister handlerRegister = new HandlerRegister();

        /*
         * 핸들러 생성 (그대로 유지)
         */
        UserHandler userHandler = new UserHandler();
        PageHandler pageHandler = new PageHandler();
        ArticleHandler articleHandler = new ArticleHandler();
        FileHandler fileHandler = new FileHandler();

        /*
         * 어노테이션 기반 등록
         */
        handlerRegister.register(userHandler);
        handlerRegister.register(pageHandler);
        handlerRegister.register(articleHandler);
        handlerRegister.register(fileHandler);

        return handlerRegister;
    }
}
