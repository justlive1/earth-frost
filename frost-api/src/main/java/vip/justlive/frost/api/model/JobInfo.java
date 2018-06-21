package vip.justlive.frost.api.model;

import lombok.Data;

/**
 * job信息
 * 
 * @author wubo
 *
 */
@Data
public class JobInfo {

  public enum STATUS {
    /**
     * 正常
     */
    NORMAL,
    /**
     * 暂停
     */
    PAUSED
  }

  public enum TYPE {
    /**
     * bean实例
     */
    BEAN,
    /**
     * 脚本
     */
    SCRIPT
  }

  public enum STRATEGY {
    /**
     * 通知
     */
    NOTIFY,
    /**
     * 重试
     */
    RETRY;
  }

  public enum MODE {
    /**
     * 简单任务
     */
    SIMPLE,
    /**
     * 延时任务
     */
    DELAY,
    /**
     * cron表达式任务
     */
    CRON;
  }

  /**
   * 编号
   */
  private String id;

  /**
   * 名称
   */
  private String name;

  /**
   * 分组
   */
  private JobGroup group;

  /**
   * 任务状态
   */
  private String status;

  /**
   * 任务类型
   */
  private String type;

  /**
   * 脚本
   */
  private String script;

  /**
   * 参数
   */
  private String param;

  /**
   * 是否自动执行
   */
  private boolean auto;

  /**
   * 失败策略
   */
  private String failStrategy;

  /**
   * 失败通知邮件地址（每个任务特别的通知人）
   */
  private String[] notifyMails;

  /**
   * 子任务id
   */
  private String[] childJobIds;

  /**
   * 超时预警时间，单位秒
   */
  private Long timeout;

  /**
   * 是否使用分片
   */
  private boolean useSharding;

  /**
   * 分片
   */
  private Integer sharding;

  /**
   * 任务模式
   */
  private String mode;

  /**
   * 简单任务的时间戳
   */
  private Long timestamp;

  /**
   * 延时任务延时
   */
  private Long initDelay;
  private Long delay;

  /**
   * 定时表达式
   */
  private String cron;


}
