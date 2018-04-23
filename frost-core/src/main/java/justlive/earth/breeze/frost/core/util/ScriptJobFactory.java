package justlive.earth.breeze.frost.core.util;

import groovy.lang.GroovyClassLoader;
import justlive.earth.breeze.frost.core.job.IJob;
import justlive.earth.breeze.snow.common.base.exception.Exceptions;

/**
 * 脚本任务工厂
 * 
 * @author wubo
 *
 */
public class ScriptJobFactory {

  ScriptJobFactory() {}

  private static final GroovyClassLoader LOADER = new GroovyClassLoader();

  /**
   * 解析脚本
   * 
   * @param script
   */
  public static IJob parse(String script) {
    Class<?> clazz = LOADER.parseClass(script);
    Object obj;
    try {
      obj = clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
    if (obj instanceof IJob) {
      return IJob.class.cast(obj);
    }
    throw Exceptions.fail("30004", "脚本未实现IJob接口");
  }

}
