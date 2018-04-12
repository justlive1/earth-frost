package justlive.earth.breeze.frost.core.dispacher;

import justlive.earth.breeze.frost.core.model.JobInfo;

/**
 * 分发接口
 * 
 * @author wubo
 *
 */
public interface Dispatcher {

  /**
   * 分发Job，失败会抛出运行时异常
   */
  void dispatch(JobInfo job);

  /**
   * 校验是否抛出异常，失败会抛出运行时异常
   * 
   * @return
   */
  String checkDispatch(JobInfo job);
}
