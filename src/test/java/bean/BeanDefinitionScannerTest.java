package bean;

import myapp.application.PageHandler;
import myapp.application.StaticResourceHandler;
import myapp.bean.BeanDefinition;
import myapp.bean.BeanDefinitionRegistry;
import myapp.bean.BeanDefinitionScanner;
import myapp.model.UserHandler;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BeanDefinitionScannerTest {

    @Test
    void 클래스로_넘겨주기() {

        /*
         * given
         * */
        BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
        BeanDefinitionScanner scanner = new BeanDefinitionScanner(registry);

        /*
         * when
         * */

        scanner.scan(UserHandler.class, StaticResourceHandler.class);

        /*
         * then
         * */
        assertThat(registry.size()).isEqualTo(2);

        assertThat(registry.contains("userHandler")).isTrue();
        assertThat(registry.contains("staticHandler")).isTrue();

        BeanDefinition userHandlerDef = registry.get("userHandler");
        assertThat(userHandlerDef.getBeanClass()).isEqualTo(UserHandler.class);

        BeanDefinition staticHandlerDef = registry.get("staticHandler");
        assertThat(staticHandlerDef.getBeanClass()).isEqualTo(StaticResourceHandler.class);
    }


    @Test
    void 패키지_기반_컴포넌트_스캔된다() {
        /*
         * given
         * */
        BeanDefinitionRegistry registry = new BeanDefinitionRegistry();
        BeanDefinitionScanner scanner =
                new BeanDefinitionScanner(registry);

        /*
         * when
         * */
        scanner.scan("myapp");

        /*
         * then
         * */
        assertThat(registry.size()).isEqualTo(3);

        assertThat(registry.contains("userHandler")).isTrue();
        assertThat(registry.contains("staticHandler")).isTrue();
        assertThat(registry.contains("pageHandler")).isTrue();

        BeanDefinition userDef = registry.get("userHandler");
        assertThat(userDef.getBeanClass())
                .isEqualTo(UserHandler.class);

        BeanDefinition staticDef = registry.get("staticHandler");
        assertThat(staticDef.getBeanClass())
                .isEqualTo(StaticResourceHandler.class);

        BeanDefinition pageDef = registry.get("pageHandler");
        assertThat(pageDef.getBeanClass())
                .isEqualTo(PageHandler.class);


        /*
         * 출력
         */
        Collection<BeanDefinition> all = registry.getAll();

        for (BeanDefinition def : all) {
            System.out.println(
                    "beanName=" + def.getBeanName() +
                            ", beanClass=" + def.getBeanClass().getName()
            );
        }

    }

}