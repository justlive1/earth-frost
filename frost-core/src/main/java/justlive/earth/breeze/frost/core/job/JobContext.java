package justlive.earth.breeze.frost.core.job;

import justlive.earth.breeze.frost.core.model.JobInfo;

/**
 * job上下文
 * 
 * @author wubo
 *
 */
public interface JobContext {

  /**
   * 获取job信息
   * 
   * @return
   */
  JobInfo getInfo();


}
