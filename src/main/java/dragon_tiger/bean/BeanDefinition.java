package dragon_tiger.bean;

public class BeanDefinition {
    private final Class<?> beanClass;
    private final String beanName;

    public BeanDefinition(Class<?> beanClass, String beanName) {
        this.beanClass = beanClass;
        this.beanName = beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getBeanName() {
        return beanName;
    }
}
