package model;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Map<String, User> sessions = new ConcurrentHashMap<>();


    public static String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();

        sessions.putIfAbsent(sessionId, user);

        return sessionId;
    }

    public static User getLoginUser(String sessionId) {
        if(sessionId==null){
            return null;
        }
        return sessions.get(sessionId);
    }
}
