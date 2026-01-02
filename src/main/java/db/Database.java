package db;

import model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private static Map<String, User> users = new ConcurrentHashMap<>();

    public static User addUser(User user) {
        User prev = users.putIfAbsent(user.getUserId(), user);

        return prev;
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
