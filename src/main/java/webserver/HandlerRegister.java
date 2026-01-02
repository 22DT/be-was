package webserver;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HandlerRegister {

    private final Map<HandlerKey, HandlerDefinition> handlers = new HashMap<>();

    /* ================= 핸들러 등록 ================= */

    public void register(HttpMethod method, String path, Object handler, String methodName, Class<?>... paramTypes) {
        try {
            Method target =
                    handler.getClass().getMethod(methodName, paramTypes);

            register(method, path, handler, target);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "핸들러 메서드 없음: " +
                            handler.getClass().getName() + "#" + methodName,
                    e
            );
        }
    }

    public void register(
            HttpMethod method,
            String path,
            Object handler,
            Method target
    ) {
        HandlerKey key = new HandlerKey(method, path);

        if (handlers.containsKey(key)) {
            throw new IllegalStateException(
                    "중복 핸들러 등록: " + key
            );
        }

        HandlerDefinition def =
                new HandlerDefinition(method, path, handler, target);

        handlers.put(key, def);
    }

    /* ================= 조회 ================= */

    public HandlerDefinition get(
            HttpMethod method,
            String path
    ) {
        return handlers.get(new HandlerKey(method, path));
    }

    public Collection<HandlerDefinition> getAll() {
        return handlers.values();
    }
}
