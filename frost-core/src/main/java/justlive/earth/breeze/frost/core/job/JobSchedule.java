package justlive.earth.breeze.frost.core.job;

/**
 * job定时处理接口
 * 
 * @author wubo
 *
 */
public interface JobSchedule {

  /**
   * 增加job
   * 
   * @param jobId
   * @param cron
   * @return
   */
  String addJob(String jobId, String cron);

  /**
   * 刷新job
   * 
   * @param jobId
   * @param cron
   * @return
   */
  String refreshJob(String jobId, String cron);

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
}
