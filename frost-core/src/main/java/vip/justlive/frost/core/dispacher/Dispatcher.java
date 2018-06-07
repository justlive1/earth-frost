package vip.justlive.frost.core.dispacher;

import vip.justlive.frost.api.model.JobExecuteParam;

/**
 * 分发接口
 * 
 * @author wubo
 *
 */
public interface Dispatcher {

  /**
   * 分发Job，失败会抛出运行时异常
   * 
   * @param param 运行参数
   */
  void dispatch(JobExecuteParam param);

  /**
   * 校验是否抛出异常，失败会抛出运行时异常
   * 
   * @param param 运行参数
   */
  void checkDispatch(JobExecuteParam param);

  /**
   * 获取执行器数量
   * 
   * @param param 运行参数
   * @return 执行器数
   */
  int count(JobExecuteParam param);
}
