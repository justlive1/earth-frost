package justlive.earth.breeze.frost.core.job;

import justlive.earth.breeze.frost.core.model.JobInfo;

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
   * @param jobInfo
   * @return
   */
  String addJob(JobInfo jobInfo);

}
