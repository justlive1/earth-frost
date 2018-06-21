package vip.justlive.frost.core.job;

/**
 * job定时处理接口
 * 
 * @author wubo
 *
 */
public interface JobSchedule {

  /**
   * 新增任务
   * 
   * @param jobId
   * @return
   */
  String addJob(String jobId);

  /**
   * 增加简单任务
   * 
   * @param jobId
   * @param timestamp 执行时间点的时间戳
   * @return
   */
  String addSimpleJob(String jobId, long timestamp);

  /**
   * 增加延迟任务
   * 
   * @param jobId
   * @param initDelay
   * @param delay
   * @return
   */
  String addDelayJob(String jobId, long initDelay, long delay);

  /**
   * 增加表达式job
   * 
   * @param jobId
   * @param cron
   * @return
   */
  String addCronJob(String jobId, String cron);

  /**
   * 刷新job
   * 
   * @param jobId
   * @param cron
   * @return
   */
  String refreshJob(String jobId, String cron);

  /**
   * 刷新job
   * 
   * @param jobId
   * @param timestamp
   * @return
   */
  String refreshJob(String jobId, Long timestamp);

  /**
   * 刷新job
   * 
   * @param jobId
   * @param initDelay
   * @param delay
   * @return
   */
  String refreshJob(String jobId, Long initDelay, Long delay);

  /**
   * 暂停job
   * 
   * @param jobId
   */
  void pauseJob(String jobId);

  /**
   * 恢复job
   * 
   * @param jobId
   * @return
   */
  String resumeJob(String jobId);

  /**
   * 删除job
   * 
   * @param jobId
   */
  void removeJob(String jobId);

  /**
   * 触发 job
   * 
   * @param jobId
   */
  void triggerJob(String jobId);

  /**
   * 失败重试 job
   * 
   * @param jobId
   * @param loggerId
   * @param parentLoggerId
   */
  void retryJob(String jobId, String loggerId, String parentLoggerId);

  /**
   * 触发子job
   * 
   * @param jobId
   * @param loggerId
   */
  void triggerChildJob(String jobId, String loggerId);
}
