package vip.justlive.frost.core.monitor;

import vip.justlive.frost.api.model.JobExecuteParam;

/**
 * 监听接口
 * 
 * @author wubo
 *
 */
public interface Monitor {

  /**
   * 监听
   * 
   * @param target 目标
   */
  void watch(JobExecuteParam target);

  /**
   * 终止监听
   * 
   * @param target 目标
   */
  void unWatch(JobExecuteParam target);
}
