package myapp.handler;

import myapp.http.HttpRequest;
import myapp.http.HttpResponse;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandlerRegister {

    private final Map<HandlerKey, HandlerDefinition> handlers = new HashMap<>();
    private final HandlerTrie trie = new HandlerTrie();

    /* ================= 핸들러 등록 ================= */

    public void register(Object handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            HandlerMapping mapping = method.getAnnotation(HandlerMapping.class);

            if (mapping == null) {
                continue;
            }

            validate(method);

            trie.register(mapping.method(), mapping.path(), handler, method);
        }
    }

    private void validate(Method method) {
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 2 ||
                params[0] != HttpRequest.class ||
                params[1] != HttpResponse.class) {

            throw new IllegalStateException("Invalid handler signature: " + method);
        }
    }

    /* ================= 조회 ================= */

    public HandlerDefinition findHandler(HttpRequest request) {
        return trie.find(request);
    }
}
