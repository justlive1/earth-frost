package vip.justlive.frost.core.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import groovy.lang.GroovyClassLoader;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 脚本任务工厂
 * 
 * @author wubo
 *
 */
public class ScriptJobFactory {

  ScriptJobFactory() {}

  private static final GroovyClassLoader LOADER = new GroovyClassLoader();

  private static final Cache<String, BaseJob> CACHE = Caffeine.newBuilder().softValues().build();

  /**
   * 解析脚本
   * 
   * @param script
   */
  public static BaseJob parse(String script) {
    Class<?> clazz = LOADER.parseClass(script);
    Object obj;
    try {
      obj = clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
    if (obj instanceof BaseJob) {
      return BaseJob.class.cast(obj);
    }
    throw Exceptions.fail("30004", "脚本未实现IJob接口");
  }

  /**
   * 解析带版本号的脚本
   * 
   * @param script
   * @param versionId
   * @return
   */
  public static BaseJob parse(String script, String versionId) {
    if (versionId == null) {
      return parse(script);
    }

    BaseJob job = CACHE.getIfPresent(versionId);
    if (job != null) {
      return job;
    }

    job = parse(script);
    CACHE.put(versionId, job);
    return job;
  }
}
