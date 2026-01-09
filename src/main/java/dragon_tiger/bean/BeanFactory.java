package dragon_tiger.bean;

import java.util.HashMap;
import java.util.Map;

public class BeanFactory {
    private final BeanDefinitionRegistry registry;
    private final Map<String, Object> singletonObjects = new HashMap<>();

    public BeanFactory(BeanDefinitionRegistry registry){
        this.registry = registry;
    }

    public Object getBean(String name){
        if(singletonObjects.containsKey(name)){
            return singletonObjects.get(name);
        }

        BeanDefinition beanDefinition = registry.get(name);

        if(beanDefinition==null){
            throw new RuntimeException("빈 정의가 존재하지 않음: " + name);
        }

        Object bean = createBean(beanDefinition);
        singletonObjects.put(name, bean);
        return bean;
    }

    /*
    * 생성 전략도
    * 1. 팩토리 메소드
    * 2. 파라미터 있는 생성자
    * 3. 파라미터 없는 생성자
    *
    * 이런 식으로 나뉠 건데 흠...
    * */
    private Object createBean(BeanDefinition beanDefinition) {
        try{
            Class<?> clazz = beanDefinition.getBeanClass();
            Object bean = clazz.getDeclaredConstructor().newInstance();

            return bean;
        }catch (Exception e){
            throw new RuntimeException("빈 생성 실패: "+beanDefinition.getBeanName(), e);
        }
    }
}
