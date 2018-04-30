package justlive.earth.breeze.frost.core.job;

/**
 * job日志
 * 
 * @author wubo
 *
 */
public interface JobLogger {

  /**
   * 绑定job日志
   * 
   * @param jobId
   */
  String bindLog(String jobId);

  /**
   * 获取job对应的日志id
   * 
   * @param jobId
   * @return
   */
  String findLoggerId(String jobId);

  /**
   * 删除job对应日志
   * 
   * @param jobId
   */
  void removeLogger(String jobId);
}
