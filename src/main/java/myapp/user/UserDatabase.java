package myapp.user;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class UserDatabase {
    private static Map<String, User> users = new ConcurrentHashMap<>();
    private static Set<String> names = new ConcurrentSkipListSet<>();

    public static User addUser(User user) {

        // 1. 아이디 중복 검사
        User prev = users.putIfAbsent(user.getUserId(), user);
        if (prev != null) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 2. 닉네임 중복 검사
        if (!names.add(user.getName())) {
            // 아이디 롤백
            users.remove(user.getUserId());
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        return user;
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
