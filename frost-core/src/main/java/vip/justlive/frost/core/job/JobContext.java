package vip.justlive.frost.core.job;

import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobSharding;

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
   * @return job信息
   */
  JobInfo getInfo();

  /**
   * 获取参数
   * 
   * @return 参数
   */
  String getParam();

  /**
   * 获取分片信息
   * 
   * @return 分片信息
   */
  JobSharding getSharding();

}
