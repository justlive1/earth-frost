package vip.justlive.frost.core.job;

/**
 * job定时处理接口
 *
 * @author wubo
 */
public interface JobSchedule {

  /**
   * 新增任务
   *
   * @param jobId job编号
   * @return taskId
   */
  String addJob(String jobId);

  /**
   * 增加简单任务
   *
   * @param jobId job编号
   * @param timestamp 执行时间点的时间戳
   * @return taskId
   */
  String addSimpleJob(String jobId, long timestamp);

  /**
   * 增加延迟任务
   *
   * @param jobId job编号
   * @param initDelay 初始延迟
   * @param delay 延迟
   * @return taskId
   */
  String addDelayJob(String jobId, long initDelay, long delay);

  /**
   * 增加表达式job
   *
   * @param jobId job编号
   * @param cron 定时表达式
   * @return taskId
   */
  String addCronJob(String jobId, String cron);

  /**
   * 刷新job
   *
   * @param jobId job编号
   * @param cron 定时表单式
   * @return taskId
   */
  String refreshJob(String jobId, String cron);

  /**
   * 刷新job
   *
   * @param jobId job编号
   * @param timestamp 时间戳
   * @return taskId
   */
  String refreshJob(String jobId, Long timestamp);

  /**
   * 刷新job
   *
   * @param jobId job编号
   * @param initDelay 初始延迟
   * @param delay 延迟
   * @return taskId
   */
  String refreshJob(String jobId, Long initDelay, Long delay);

  /**
   * 暂停job
   *
   * @param jobId job编号
   */
  void pauseJob(String jobId);

  /**
   * 恢复job
   *
   * @param jobId job编号
   * @return taskId
   */
  String resumeJob(String jobId);

  /**
   * 删除job
   *
   * @param jobId job编号
   */
  void removeJob(String jobId);

  /**
   * 触发 job
   *
   * @param jobId job编号
   */
  void triggerJob(String jobId);

  /**
   * 失败重试 job
   *
   * @param jobId job编号
   * @param loggerId 日志id
   * @param parentLoggerId 父任务日志id
   */
  void retryJob(String jobId, String loggerId, String parentLoggerId);

  /**
   * 触发子job
   *
   * @param jobId job编号
   * @param loggerId 日志id
   */
  void triggerChildJob(String jobId, String loggerId);
}
