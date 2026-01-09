package webserver;

import myapp.handler.HandlerDefinition;
import myapp.handler.HandlerRegister;
import myapp.http.HttpMethod;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;
import myapp.model.UserHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HandlerRegisterTest {

    @Test
    void POST_create_요청에_대한_핸들러를_조회한다() {
        // given
        HandlerRegister handlerRegister = new HandlerRegister();
        UserHandler userHandler = new UserHandler();

        handlerRegister.register(
                HttpMethod.POST,
                "/create",
                userHandler,
                "register",
                HttpRequest.class,
                HttpResponse.class
        );

        // when
        HandlerDefinition handler =
                handlerRegister.get(HttpMethod.POST, "/create");

        // then
        assertNotNull(handler);
        assertEquals(HttpMethod.POST, handler.getMethod());
        assertEquals("/create", handler.getPath());
        assertEquals(userHandler, handler.getHandler());
        assertEquals("register", handler.getTarget().getName());
    }

}