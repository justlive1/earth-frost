package vip.justlive.frost.core.job;

/**
 * job日志
 *
 * @author wubo
 */
public interface JobLogger {

  /**
   * 绑定job日志
   *
   * @param jobId job编号
   * @return 日志id
   */
  String bindLog(String jobId);

  /**
   * 删除job对应日志
   *
   * @param jobId job编号
   */
  void removeLogger(String jobId);

  /**
   * 开始调度或执行任务
   *
   * @param loggerId 日志id
   * @param type 类型
   */
  void enter(String loggerId, String type);

  /**
   * 调度或执行任务结束
   *
   * @param loggerId 日志id
   * @param type 类型
   * @param success 是否成功
   */
  void leave(String loggerId, String type, boolean success);
}
