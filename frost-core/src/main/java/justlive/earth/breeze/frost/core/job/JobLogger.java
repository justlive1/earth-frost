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
   * 删除job对应日志
   * 
   * @param jobId
   */
  void removeLogger(String jobId);
}
