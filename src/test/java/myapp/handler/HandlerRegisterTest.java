package myapp.handler;

import myapp.http.HttpMethod;
import myapp.http.HttpRequest;
import myapp.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HandlerRegisterTest {


    @Test
    void givenStaticPath_whenRequestMatches_thenStaticHandlerIsCalled() {
        // given
        HandlerRegister register = new HandlerRegister();
        TestArticleHandler handler = new TestArticleHandler();
        register.register(handler);

        HttpRequest request =
                request(HttpMethod.GET, "/articles/new");

        // when

        HandlerDefinition def = register.findHandler(request);
        def.handle(request, new HttpResponse());

        // then
        assertTrue(handler.called);
        assertNull(handler.articleId);

    }

    @Test
    void givenStaticAndVariablePath_whenStaticMatches_thenStaticHasPriority() {
        // given
        HandlerRegister register = new HandlerRegister();
        TestArticleHandler handler = new TestArticleHandler();
        register.register(handler);

        HttpRequest request =
                request(HttpMethod.GET, "/articles/new");

        // when
        HandlerDefinition def = register.findHandler(request);
        def.handle(request, new HttpResponse());

        // then
        assertTrue(handler.called);
        assertNull(handler.articleId);
    }

    @Test
    void givenSinglePathVariable_whenMatched_thenVariableIsExtracted() {
        // given
        HandlerRegister register = new HandlerRegister();
        TestArticleHandler handler = new TestArticleHandler();
        register.register(handler);

        HttpRequest request =
                request(HttpMethod.GET, "/articles/42");

        // when
        HandlerDefinition def = register.findHandler(request);
        def.handle(request, new HttpResponse());

        // then
        assertTrue(handler.called);
        assertEquals("42", handler.articleId);
    }


    @Test
    void givenMultiplePathVariables_whenMatched_thenAllVariablesAreExtracted() {
        // given
        HandlerRegister register = new HandlerRegister();
        TestArticleHandler handler = new TestArticleHandler();
        register.register(handler);

        HttpRequest request =
                request(HttpMethod.GET, "/articles/10/comments/3");

        // when
        HandlerDefinition def = register.findHandler(request);
        def.handle(request, new HttpResponse());

        // then
        assertTrue(handler.called);
        assertEquals("10", handler.articleId);
        assertEquals("3", handler.commentId);
    }

    @Test
    void givenPathDepthMismatch_whenRequest_thenHandlerNotFound() {
        // given
        HandlerRegister register = new HandlerRegister();
        register.register(new TestArticleHandler());

        HttpRequest request =
                request(HttpMethod.GET, "/articles/10/comments");

        // when
        HandlerDefinition def = register.findHandler(request);

        // then
        assertNull(def);
    }


    @Test
    void givenPathMatchesButMethodDoesNot_whenRequest_thenMethodNotAllowed() {
        // given
        HandlerRegister register = new HandlerRegister();
        register.register(new TestArticleHandler());

        HttpRequest request =
                request(HttpMethod.POST, "/articles/10");

        // when / then
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> register.findHandler(request)
        );

        assertEquals("405 Method Not Allowed", ex.getMessage());
    }

    @Test
    void givenUnknownPath_whenRequest_thenHandlerNotFound() {
        // given
        HandlerRegister register = new HandlerRegister();
        register.register(new TestArticleHandler());

        HttpRequest request =
                request(HttpMethod.GET, "/unknown/path");

        // when
        HandlerDefinition def = register.findHandler(request);

        // then
        assertNull(def);
    }


    @Test
    void givenDuplicateHandlerMapping_whenRegister_thenExceptionThrown() {
        // given
        HandlerRegister register = new HandlerRegister();

        TestArticleHandler handler1 = new TestArticleHandler();
        TestArticleHandler handler2 = new TestArticleHandler();

        register.register(handler1);

        // when / then
        assertThrows(
                IllegalStateException.class,
                () -> register.register(handler2)
        );
    }


    private HttpRequest request(HttpMethod method, String path) {
        return new HttpRequest(
                method,
                path,
                Map.of(),           // queryParams
                "HTTP/1.1",
                Map.of(),           // headers
                null                // body
        );
    }

}