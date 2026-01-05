package model;

import db.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class UserHandlerTest {

    private UserHandler userHandler;

    @BeforeEach
    void suetUp() {
        userHandler = new UserHandler();
    }


    @Test
    void 정상_회원가입() {
        /*
         * given
         * */

        HttpRequest request = requestWith(
                "userId", "user1",
                "password", "pw",
                "name", "kim",
                "email", "kim@test.com"
        );
        HttpResponse response = new HttpResponse();



        /*
         * when
         * */

        assertDoesNotThrow(() -> userHandler.register(request, response));


        /*
         * then
         * */

        User saved = Database.findUserById("user1");
        assertNotNull(saved);
        assertEquals("user1", saved.getUserId());

    }

    @Test
    void 필수_파라미터_누락() {

        /*
         * given
         * */

        HttpRequest request = requestWith(
                "userId", "user1",
                "password", "pw",
                "name", "kim"
        );
        HttpResponse response = new HttpResponse();


        /*
         * when / then
         * */

        assertThrows(IllegalArgumentException.class,
                () -> userHandler.register(request, response));

        assertNull(Database.findUserById("user1"));

    }

    @Test
    void 중복_userId_single_thread() {

        /*
         * given
         * */

        HttpRequest request1 = requestWith(
                "userId", "user1",
                "password", "pw",
                "name", "kim",
                "email", "kim@test.com"
        );
        HttpRequest request2 = requestWith(
                "userId", "user1",
                "password", "pw2",
                "name", "lee",
                "email", "lee@test.com"
        );


        /*
         * when
         * */

        userHandler.register(request1, new HttpResponse());


        /*
         * then
         * */

        assertThrows(IllegalArgumentException.class,
                () -> userHandler.register(request2, new HttpResponse()));

        assertEquals(1, Database.findAll().size());

    }


    @Test
    void 중복_userId_multi_thread() throws InterruptedException {

        /*
         * given
         * */

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        Runnable task = () -> {
            try {
                userHandler.register(
                        requestWith(
                                "userId", "user1",
                                "password", "pw",
                                "name", "kim",
                                "email", "kim@test.com"
                        ),
                        new HttpResponse()
                );
            } catch (Exception ignored) {
            }
        };


        /*
         * when
         * */
        for (int i = 0; i < threadCount; i++) {
            executor.submit(task);
        }


        /*
         * 더 이상 새로운 작ㅇ버을 받지 않고
         * 이미 제출된 모든 작업이 끝날 때까지 기다린다.
         * 스레드 풀을 정상적으로 종료하지 않으면 테스트가 끝나지 않을 수 있음
         * */

        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);


        /*
         * then
         * */

        assertEquals(1, Database.findAll().size());

    }


    private HttpRequest requestWith(String... kvs) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            params.put(kvs[i], kvs[i + 1]);
        }

        return new HttpRequest(
                HttpMethod.GET,      // 테스트 기본값
                "/create",           // path
                params,              // queryParams
                "HTTP/1.1",          // version
                new HashMap<>(),      // headers
                null
        );
    }


}