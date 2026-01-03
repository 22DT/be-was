package model;

import db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.WebServer;

public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

    public void register(HttpRequest request, HttpResponse response) {
        logger.debug("[register]");

        /*
        * request 처리
        * */

        String userId = request.getRequiredParam("userId");
        String password = request.getRequiredParam("password");
        String name = request.getRequiredParam("name");
        String email = request.getRequiredParam("email");

        User user = new User(userId, password, name, email);


        /*
        * 비즈니스 로직
        * */

        User prev = Database.addUser(user);

        if (prev != null) {
            throw new IllegalArgumentException(
                    "이미 존재하는 userId: " + user.getUserId()
            );
        }


        /*
        * response
        * */


    }

    public void login(HttpRequest request, HttpResponse response) {

        /*
        * request
        * */

        String userId=null;
        String password=null;


        /*
        * 비즈니스 로직
        * */

        User user = Database.findUserById(userId);

        // 1. 아이디 없음
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        // 2. 비밀번호 불일치
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 로그인 성공
        // (현재는 성공 처리만, 세션/상태 없음)

        
        /*
        * response
        * */
    }
}
