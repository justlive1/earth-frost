package vip.justlive.frost.core.config;

import lombok.Data;
import vip.justlive.oxygen.core.config.ValueConfig;

/**
 * 执行器相关配置
 *
 * @author wubo
 */
@Data
@ValueConfig("frost.executor")
public class JobExecutorProperties {

  /**
   * 执行器名称
   */
  private String name;

  /**
   * 执行器Key
   */
  private String key;

  /**
   * 执行器部署ip
   */
  private String ip;

  /**
   * 执行器监听端口
   */
  private Integer port;

  /**
   * 是否开启脚本任务执行
   */
  private Boolean scriptJobEnabled = true;

  /**
   * 错过执行的阈值(毫秒)
   */
  private long misfireThreshold = 5000L;

  /**
   * 最大日志数
   */
  private long maxLogSize = -1;

}
