package justlive.earth.breeze.frost.core.model;

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
   * 定时表达式
   */
  private String cron;

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
   * 任务Id
   */
  private String taskId;

  /**
   * 日志Id
   */
  private String logId;

}
