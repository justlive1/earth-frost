package justlive.earth.breeze.frost.core.service;

import java.util.List;
import justlive.earth.breeze.frost.core.model.JobExecutor;

/**
 * 调度中心服务接口
 * 
 * @author wubo
 *
 */
public interface CenterService {

  /**
   * 获取激活的执行器列表
   * 
   * @return
   */
  List<JobExecutor> queryActiveExecutors();
}
