package model;

import db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.WebServer;

public class UserHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

    public void register(User user) {
        logger.debug("[register]");
        User prev = Database.addUser(user);

        if (prev != null) {
            throw new IllegalArgumentException(
                    "이미 존재하는 userId: " + user.getUserId()
            );
        }
    }

    public void login(String userId, String password) {
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
    }
}
