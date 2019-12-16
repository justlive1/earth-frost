package vip.justlive.frost.core.util;

import groovy.lang.GroovyClassLoader;
import java.util.concurrent.TimeUnit;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ExpiringMap;

/**
 * 脚本任务工厂
 *
 * @author wubo
 */
public class ScriptJobFactory {

  ScriptJobFactory() {
  }

  private static final GroovyClassLoader LOADER = new GroovyClassLoader();

  private static final ExpiringMap<String, BaseJob> CACHE =
      ExpiringMap.<String, BaseJob>builder().expiration(1, TimeUnit.DAYS).build();

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
      return (BaseJob) obj;
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

    BaseJob job = CACHE.get(versionId);
    if (job != null) {
      return job;
    }

    job = parse(script);
    CACHE.put(versionId, job);
    return job;
  }
}
