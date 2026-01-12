package myapp.handler;

import myapp.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class HandlerNode {
    // 고정 경로 조각
    final Map<String, HandlerNode> staticChildren = new HashMap<>();
    // Path Variable 전용 노드
    HandlerNode variableChild;
    String variableName;

    // 경로 + HTTP method의 최종 목적지
    Map<HttpMethod, HandlerDefinition> handlers = new HashMap<>();

    HandlerNode getOrCreateStatic(String segment) {
        return staticChildren.computeIfAbsent(segment, s -> new HandlerNode());
    }

    HandlerNode getOrCreateVariable(String segment) {
        if (variableChild == null) {
            variableChild = new HandlerNode();
            variableName = segment.substring(1, segment.length() - 1);
        }
        return variableChild;
    }

}
