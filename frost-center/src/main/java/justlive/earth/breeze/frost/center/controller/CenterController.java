package justlive.earth.breeze.frost.center.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.service.CenterService;
import justlive.earth.breeze.snow.common.base.domain.Response;

/**
 * 调度中心Controller
 * 
 * @author wubo
 *
 */
@RestController
public class CenterController {

  @Autowired
  CenterService centerService;

  /**
   * 获取当前执行器列表
   * 
   * @return
   */
  @RequestMapping("/queryExecutors")
  public Response<List<JobExecutor>> queryExecutors() {
    List<JobExecutor> list = centerService.queryActiveExecutors();
    return Response.success(list);
  }

}
