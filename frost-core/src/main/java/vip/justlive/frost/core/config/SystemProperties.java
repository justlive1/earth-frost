package vip.justlive.frost.core.config;

import lombok.Data;
import vip.justlive.oxygen.core.config.ValueConfig;

/**
 * 系统属性
 * 
 * @author wubo
 *
 */
@Data
@ValueConfig("system")
public class SystemProperties {

  /**
   * 线程池中最小线程数
   */
  private Integer corePoolSize = 5;

  /**
   * 最大线程数
   */
  private Integer maximumPoolSize = 50;

  /**
   * 空闲线程待机时间
   */
  private Integer keepAliveTime = 300;

  /**
   * 待执行线程队列
   */
  private Integer queueCapacity = 200;

  private Integer workers = 10;

}
