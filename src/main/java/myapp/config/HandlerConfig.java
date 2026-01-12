package myapp.config;

import myapp.application.PageHandler;
import myapp.handler.HandlerRegister;
import myapp.model.UserHandler;

public class HandlerConfig {
    public static HandlerRegister initialize() {
        HandlerRegister handlerRegister = new HandlerRegister();

        /*
         * 핸들러 생성 (그대로 유지)
         */
        UserHandler userHandler = new UserHandler();
        PageHandler pageHandler = new PageHandler();

        /*
         * 어노테이션 기반 등록
         */
        handlerRegister.register(userHandler);
        handlerRegister.register(pageHandler);

        return handlerRegister;
    }
}
