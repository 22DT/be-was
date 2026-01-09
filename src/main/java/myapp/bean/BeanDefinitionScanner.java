package myapp.bean;

import java.io.File;
import java.net.URL;

public class BeanDefinitionScanner {
    private final BeanDefinitionRegistry registry;
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public BeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public void scan(String basePackage) {
        String path = basePackage.replace('.', '/');

        try {
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                throw new RuntimeException("패키를 찾을 수 없음: " + basePackage);
            }

            File baseDir = new File(resource.getFile());
            scanDirectory(baseDir, basePackage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scanDirectory(File dir, String packageName) throws ClassNotFoundException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + '.' + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().replace(".class", "");

                Class<?> clazz = Class.forName(packageName + "." + className);

                registerIfComponent(clazz);

            }
        }
    }

    private void registerIfComponent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            return;
        }

        String beanName = BeanNameGenerator.generate(clazz);
        BeanDefinition definition =
                new BeanDefinition(clazz, beanName);

        registry.register(definition);
    }

    public void scan(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotationPresent(Component.class)) {
                continue;
            }

            String beanName = BeanNameGenerator.generate(clazz);

            BeanDefinition definition = new BeanDefinition(clazz, beanName);

            registry.register(definition);
        }
    }
}
