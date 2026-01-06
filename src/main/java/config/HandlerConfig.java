package config;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.UserHandler;
import handler.HandlerRegister;
import handler.PageHandler;

public class HandlerConfig {
    public static HandlerRegister initialize(){
        HandlerRegister handlerRegister = new HandlerRegister();

        /*
         * 필요한 헨들러들 생성
         * */

        UserHandler userHandler = new UserHandler();
        PageHandler pageHandler = new PageHandler();

        /*
         * 등록
         * */

        // 회원가입
        handlerRegister.register(HttpMethod.POST, "/user/create", userHandler, "register", HttpRequest.class, HttpResponse.class);
        // 로그인
        handlerRegister.register(HttpMethod.POST, "/user/login", userHandler, "login", HttpRequest.class, HttpResponse.class);
        // 기본 페이지
        handlerRegister.register(HttpMethod.GET, "/index.html", pageHandler, "index", HttpRequest.class, HttpResponse.class);
        // 마이 페이지
        handlerRegister.register(HttpMethod.GET, "/mypage", pageHandler, "myPage", HttpRequest.class, HttpResponse.class);

        return handlerRegister;
    }

    public static HandlerRegister initializeWithAnnotations() {
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
