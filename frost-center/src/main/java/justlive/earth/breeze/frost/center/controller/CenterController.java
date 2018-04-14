package justlive.earth.breeze.frost.center.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.service.JobService;
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
  JobService jobService;

  /**
   * 获取当前执行器列表
   * 
   * @return
   */
  @RequestMapping("/queryExecutors")
  public Response<List<JobExecutor>> queryExecutors() {
    List<JobExecutor> list = jobService.queryExecutors();
    return Response.success(list);
  }

  /**
   * 增加job
   * 
   * @param jobInfo
   * @return
   */
  @RequestMapping("/addJob")
  public Response<String> addJob(@RequestBody JobInfo jobInfo) {
    String jobId = jobService.addJob(jobInfo);
    return Response.success(jobId);
  }

  /**
   * 获取job列表
   * 
   * @return
   */
  @RequestMapping("/queryJobInfos")
  public Response<List<JobInfo>> queryJobInfos() {
    List<JobInfo> jobInfos = jobService.queryJobInfos();
    return Response.success(jobInfos);
  }

  /**
   * 获取job执行记录列表
   * 
   * @param jobId
   * @param from
   * @param to
   * @return
   */
  @RequestMapping("/queryJobExecuteRecords")
  public Response<List<JobExecuteRecord>> queryJobExecuteRecords(String jobId, int from, int to) {
    List<JobExecuteRecord> records = jobService.queryJobRecords(jobId, from, to);
    return Response.success(records);
  }

}
