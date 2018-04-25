package justlive.earth.breeze.frost.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.BeanFactory;
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

  static final ConcurrentMap<Class<?>, BeanFactory> beanFactoryMap = new ConcurrentHashMap<>();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    beanFactoryMap.put(ApplicationContext.class, applicationContext);
  }

  public static <T> T getBean(String name, Class<T> clazz) {
    return beanFactoryMap.get(ApplicationContext.class).getBean(name, clazz);
  }

  public static <T> T getBean(Class<T> clazz) {
    return beanFactoryMap.get(ApplicationContext.class).getBean(clazz);
  }

}
