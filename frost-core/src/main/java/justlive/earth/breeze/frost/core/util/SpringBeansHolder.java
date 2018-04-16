package justlive.earth.breeze.frost.core.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring beans 工具
 * 
 * @author wubo
 *
 */
@Component
public class SpringBeansHolder implements ApplicationContextAware {

  static ApplicationContext CTX;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    CTX = applicationContext;
  }

  public static <T> T getBean(String name, Class<T> clazz) {
    return CTX.getBean(name, clazz);
  }

  public static <T> T getBean(Class<T> clazz) {
    return CTX.getBean(clazz);
  }

}
