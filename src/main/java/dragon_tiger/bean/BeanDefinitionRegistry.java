package dragon_tiger.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BeanDefinitionRegistry {
    private final Map<String, BeanDefinition> definitions = new HashMap<>();

    public void register(BeanDefinition definition) {
        String beanName = definition.getBeanName();

        if (definitions.containsKey(beanName)) {
            throw new RuntimeException("중복 Bean 이름: " + beanName);
        }

        definitions.put(beanName, definition);
    }

    public Collection<BeanDefinition> getAll() {
        return definitions.values();
    }

    public BeanDefinition get(String beanName) {
        return definitions.get(beanName);
    }

    public boolean contains(String beanName) {
        return definitions.containsKey(beanName);
    }

    public int size() {
        return definitions.size();
    }
}
