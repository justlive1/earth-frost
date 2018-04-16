package justlive.earth.breeze.frost.center.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
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
   * 登录页面
   * 
   * @return
   */
  @RequestMapping("/login")
  public ModelAndView login() {

    return new ModelAndView("login/login.html");
  }

  /**
   * 首页
   * 
   * @return
   */
  @RequestMapping({"/", "index"})
  public ModelAndView index() {

    return new ModelAndView("index.html");
  }


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
   * 修改job
   * 
   * @param jobInfo
   * @return
   */
  @RequestMapping("/updateJob")
  public Response<String> updateJob(@RequestBody JobInfo jobInfo) {
    jobService.updateJob(jobInfo);
    return Response.success("修改成功");
  }

  /**
   * 暂停job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/pauseJob")
  public Response<String> pauseJob(String id) {
    jobService.pauseJob(id);
    return Response.success("暂停成功");
  }

  /**
   * 恢复job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/resumeJob")
  public Response<String> resumeJob(String id) {
    jobService.resumeJob(id);
    return Response.success("恢复成功");
  }

  /**
   * 删除job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/removeJob")
  public Response<String> removeJob(String id) {
    jobService.removeJob(id);
    return Response.success("删除成功");
  }

  /**
   * 触发job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/triggerJob")
  public Response<String> triggerJob(String id) {
    jobService.triggerJob(id);
    return Response.success("触发成功");
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
