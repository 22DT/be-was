package db;

import model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private static Map<String, User> users = new ConcurrentHashMap<>();

    public static void addUser(User user) {
        User prev = users.putIfAbsent(user.getUserId(), user);
        if (prev != null) {
            throw new IllegalArgumentException(
                    "이미 존재하는 userId: " + user.getUserId()
            );
        }
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
