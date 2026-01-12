package myapp.handler;

import myapp.http.HttpMethod;
import myapp.http.HttpRequest;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HandlerTrie {
    private final HandlerNode root = new HandlerNode();

    public void register(HttpMethod method, String path, Object handler, Method target) {
        String[] segments = tokenize(path);
        HandlerNode node = root;

        for (String segment : segments) {
            if (isVariable(segment)) {
                node = node.getOrCreateVariable(segment);
            } else {
                node = node.getOrCreateStatic(segment);
            }
        }

        if (node.handlers.containsKey(method)) {
            throw new IllegalStateException("Duplicate route");
        }

        node.handlers.put(method, new HandlerDefinition(method, handler, target));
    }

    public HandlerDefinition find(HttpRequest request) {
        String[] segments = tokenize(request.getPath());
        HandlerNode node = root;
        Map<String, String> pathVars = new HashMap<>();

        for (String segment : segments) {
            HandlerNode next = node.staticChildren.get(segment);
            if (next != null) {
                node = next;
                continue;
            }

            if (node.variableChild != null) {
                pathVars.put(node.variableName, segment);
                node = node.variableChild;
                continue;
            }

            // 중간 경로 불일치 → 그냥 못 찾음
            return null;
        }

        //  경로는 맞았지만 endpoint 아님
        if (node.handlers.isEmpty()) {
            return null;
        }

        HandlerDefinition def = node.handlers.get(request.getMethod());
        if (def == null) {
            //  이건 "경로는 있는데 메서드만 없음" → 405는 유지
            throw new IllegalStateException("405 Method Not Allowed");
        }

        request.setPathVariables(pathVars);
        return def;
    }


    public boolean isVariable(String segment) {
        return segment.startsWith("{") && segment.endsWith("}");
    }

    private String[] tokenize(String path) {
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
