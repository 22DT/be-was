package dragon_tiger.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanFactoryTest {
    private BeanDefinitionRegistry registry;
    private BeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        registry = new BeanDefinitionRegistry();
        beanFactory = new BeanFactory(registry);
    }

    @Test
    void bean을_정의하고_가져올_수_있다() {
        // given
        BeanDefinition definition =
                new BeanDefinition(TestBean.class, "testBean");
        registry.register(definition);

        // when
        Object bean = beanFactory.getBean("testBean");

        // then
        assertNotNull(bean);
        assertInstanceOf(TestBean.class, bean);
    }

    @Test
    void 같은_이름으로_두번_조회하면_같은_객체를_반환한다() {
        // given
        BeanDefinition definition =
                new BeanDefinition(TestBean.class, "testBean");
        registry.register(definition);

        // when
        Object bean1 = beanFactory.getBean("testBean");
        Object bean2 = beanFactory.getBean("testBean");

        // then
        assertSame(bean1, bean2);
    }

    @Test
    void 정의되지_않은_빈을_요청하면_예외가_발생한다() {
        // when & then
        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> beanFactory.getBean("noSuchBean"));

        assertTrue(exception.getMessage().contains("빈 정의가 존재하지 않음"));
    }
}