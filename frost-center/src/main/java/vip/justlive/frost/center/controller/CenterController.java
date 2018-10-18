package vip.justlive.frost.center.controller;

import java.util.List;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobScript;
import vip.justlive.frost.api.model.Page;
import vip.justlive.frost.core.service.JobService;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.exception.ErrorCode;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * 调度中心Controller
 * 
 * @author wubo
 *
 */
@RestController
public class CenterController {

  private JobService jobService;

  public CenterController() {
    jobService = BeanStore.getBean(JobService.class);
  }

  /**
   * 登录页面
   * 
   * @return
   */
  @RequestMapping("/login.html")
  public ModelAndView login() {

    return new ModelAndView("login/login.html");
  }

  /**
   * 首页
   * 
   * @return
   */
  @RequestMapping({"/", "index.html"})
  public ModelAndView index() {

    return new ModelAndView("index.html");
  }


  /**
   * 获取当前执行器列表
   * 
   * @return
   */
  @RequestMapping("/queryExecutors")
  public Resp queryExecutors() {
    List<JobExecutor> list = jobService.queryExecutors();
    return Resp.success(list);
  }

  /**
   * 增加job
   * 
   * @param jobInfo
   * @return
   */
  @RequestMapping("/addJob")
  public Resp addJob(@RequestBody JobInfo jobInfo) {
    String jobId = jobService.addJob(jobInfo);
    return Resp.success(jobId);
  }

  /**
   * 修改job
   * 
   * @param jobInfo
   * @return
   */
  @RequestMapping("/updateJob")
  public Resp updateJob(@RequestBody JobInfo jobInfo) {
    jobService.updateJob(jobInfo);
    return Resp.success("修改成功");
  }

  /**
   * 暂停job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/pauseJob")
  public Resp pauseJob(String id) {
    jobService.pauseJob(id);
    return Resp.success("暂停成功");
  }

  /**
   * 恢复job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/resumeJob")
  public Resp resumeJob(String id) {
    jobService.resumeJob(id);
    return Resp.success("恢复成功");
  }

  /**
   * 删除job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/removeJob")
  public Resp removeJob(String id) {
    jobService.removeJob(id);
    return Resp.success("删除成功");
  }

  /**
   * 触发job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/triggerJob")
  public Resp triggerJob(String id) {
    jobService.triggerJob(id);
    return Resp.success("触发成功");
  }


  /**
   * 获取job列表
   * 
   * @return
   */
  @RequestMapping("/queryJobInfos")
  public Resp queryJobInfos(@RequestParam(defaultValue = "1") int pageIndex,
      @RequestParam(defaultValue = "10") int pageSize) {
    Page<JobInfo> jobInfos = jobService.queryJobInfos(pageIndex, pageSize);
    return Resp.success(jobInfos);
  }

  /**
   * 获取所有任务
   * 
   * @param pageIndex
   * @param pageSize
   * @return
   */
  @RequestMapping("/queryAllJobs")
  public Resp queryAllJobs() {
    List<JobInfo> jobInfos = jobService.queryAllJobs();
    return Resp.success(jobInfos);
  }

  /**
   * 获取job
   * 
   * @param id
   * @return
   */
  @RequestMapping("/findJobInfoById")
  public Resp findJobInfoById(String id) {
    return Resp.success(jobService.findJobInfoById(id));
  }

  /**
   * 获取job执行记录列表
   * 
   * @param groupKey
   * @param jobKey
   * @param jobId
   * @param from
   * @param to
   * @return
   */
  @RequestMapping("/queryJobExecuteRecords")
  public Resp queryJobExecuteRecords(String groupKey, String jobKey, String jobId,
      @RequestParam(defaultValue = "1") int pageIndex,
      @RequestParam(defaultValue = "10") int pageSize) {
    Page<JobExecuteRecord> records =
        jobService.queryJobRecords(groupKey, jobKey, jobId, pageIndex, pageSize);
    return Resp.success(records);
  }

  /**
   * 增加任务脚本
   * 
   * @param script
   * @return
   */
  @RequestMapping("/addJobScript")
  public Resp addJobScript(@RequestBody JobScript script) {
    jobService.addJobScript(script);
    return Resp.success("操作成功");
  }

  /**
   * 查询任务脚本
   * 
   * @param jobId
   * @return
   */
  @RequestMapping("/queryJobScripts")
  public Resp queryJobScripts(String jobId) {
    return Resp.success(jobService.queryJobScripts(jobId));
  }

  /**
   * 任务统计
   * 
   * @param begin
   * @param end
   * @return
   */
  @RequestMapping("/queryJobStatictis")
  public Resp queryJobStatictis(String begin, String end) {
    return Resp.success(jobService.queryJobStatictis(begin, end));
  }

  @ExceptionHandler({Exception.class})
  public Resp exception(Exception ex) {
    if (CodedException.class.isInstance(ex)) {
      ErrorCode errorCode = ((CodedException) ex).getErrorCode();
      return Resp.error(errorCode.getCode(), errorCode.getMessage());
    }
    return Resp.error(ex.getMessage());
  }
}
