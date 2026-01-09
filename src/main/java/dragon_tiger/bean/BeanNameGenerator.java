package dragon_tiger.bean;

public class BeanNameGenerator {

    public static String generate(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }

        String name = clazz.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}

