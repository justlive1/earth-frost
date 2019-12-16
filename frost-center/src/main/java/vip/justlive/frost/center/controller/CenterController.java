package vip.justlive.frost.center.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.redisson.executor.CronExpression;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobInfo.MODE;
import vip.justlive.frost.api.model.JobScript;
import vip.justlive.frost.api.model.Page;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.service.JobService;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.exception.ErrorCode;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.Resp;

/**
 * 调度中心Controller
 *
 * @author wubo
 */
@Slf4j
@RestController
public class CenterController {

  private final JobService jobService;

  public CenterController(Container container) {
    jobService = container.getJobService();
  }

  /**
   * 登录页面
   *
   * @return view
   */
  @RequestMapping("/login.html")
  public ModelAndView login() {
    return new ModelAndView("login/login.html");
  }

  /**
   * 首页
   *
   * @return view
   */
  @RequestMapping({"/", "index.html"})
  public ModelAndView index() {

    return new ModelAndView("index.html");
  }


  /**
   * 获取当前执行器列表
   *
   * @return executors
   */
  @RequestMapping("/queryExecutors")
  public Resp queryExecutors() {
    List<JobExecutor> list = jobService.queryExecutors();
    return Resp.success(list);
  }

  /**
   * 增加job
   *
   * @param jobInfo job信息
   * @return jobId
   */
  @RequestMapping("/addJob")
  public Resp addJob(@RequestBody JobInfo jobInfo) {
    check(jobInfo);
    String jobId = jobService.addJob(jobInfo);
    return Resp.success(jobId);
  }

  /**
   * 修改job
   *
   * @param jobInfo job信息
   * @return resp
   */
  @RequestMapping("/updateJob")
  public Resp updateJob(@RequestBody JobInfo jobInfo) {
    check(jobInfo);
    if (jobInfo.getChildJobIds() != null && Arrays.asList(jobInfo.getChildJobIds())
        .contains(jobInfo.getId())) {
      throw Exceptions.fail("子任务不能包含本任务");
    }
    jobService.updateJob(jobInfo);
    return Resp.success("修改成功");
  }

  /**
   * 暂停job
   *
   * @param id job编号
   * @return resp
   */
  @RequestMapping("/pauseJob")
  public Resp pauseJob(String id) {
    jobService.pauseJob(id);
    return Resp.success("暂停成功");
  }

  /**
   * 恢复job
   *
   * @param id job编号
   * @return resp
   */
  @RequestMapping("/resumeJob")
  public Resp resumeJob(String id) {
    jobService.resumeJob(id);
    return Resp.success("恢复成功");
  }

  /**
   * 删除job
   *
   * @param id job编号
   * @return resp
   */
  @RequestMapping("/removeJob")
  public Resp removeJob(String id) {
    jobService.removeJob(id);
    return Resp.success("删除成功");
  }

  /**
   * 触发job
   *
   * @param id job编号
   * @return resp
   */
  @RequestMapping("/triggerJob")
  public Resp triggerJob(String id) {
    jobService.triggerJob(id);
    return Resp.success("触发成功");
  }


  /**
   * 获取job列表
   *
   * @return infos
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
   * @return jobs
   */
  @RequestMapping("/queryAllJobs")
  public Resp queryAllJobs() {
    List<JobInfo> jobInfos = jobService.queryAllJobs();
    return Resp.success(jobInfos);
  }

  /**
   * 获取job
   *
   * @param id job编号
   * @return jobInfo
   */
  @RequestMapping("/findJobInfoById")
  public Resp findJobInfoById(String id) {
    return Resp.success(jobService.findJobInfoById(id));
  }

  /**
   * 获取job执行记录列表
   *
   * @param groupKey group key
   * @param jobKey job key
   * @param jobId job编号
   * @param pageIndex 第几页
   * @param pageSize 每页条数
   * @return page
   */
  @RequestMapping("/queryJobExecuteRecords")
  public Resp queryJobExecuteRecords(String groupKey, String jobKey, String jobId,
      @RequestParam(defaultValue = "1") int pageIndex,
      @RequestParam(defaultValue = "10") int pageSize) {
    Page<JobExecuteRecord> records = jobService
        .queryJobRecords(groupKey, jobKey, jobId, pageIndex, pageSize);
    return Resp.success(records);
  }

  /**
   * 增加任务脚本
   *
   * @param script 脚本
   * @return resp
   */
  @RequestMapping("/addJobScript")
  public Resp addJobScript(@RequestBody JobScript script) {
    jobService.addJobScript(script);
    return Resp.success("操作成功");
  }

  /**
   * 查询任务脚本
   *
   * @param jobId job编号
   * @return script
   */
  @RequestMapping("/queryJobScripts")
  public Resp queryJobScripts(String jobId) {
    return Resp.success(jobService.queryJobScripts(jobId));
  }

  /**
   * 任务统计
   *
   * @param begin 开始
   * @param end 结束
   * @return 统计信息
   */
  @RequestMapping("/queryJobStatistic")
  public Resp queryJobStatistic(String begin, String end) {
    return Resp.success(jobService.queryJobStatistic(begin, end));
  }

  /**
   * 删除日志
   *
   * @param jobId 任务编号
   * @return resp
   */
  @RequestMapping("/removeJobRecords")
  public Resp removeJobRecords(@RequestParam String jobId) {
    jobService.removeJobRecords(jobId);
    return Resp.success("删除成功");
  }

  @ExceptionHandler({Exception.class})
  public Resp exception(Exception ex) {
    log.error("error", ex);
    if (ex instanceof CodedException) {
      ErrorCode errorCode = ((CodedException) ex).getErrorCode();
      return Resp.error(errorCode.getCode(), errorCode.getMessage());
    }
    return Resp.error(ex.getMessage());
  }

  private void check(JobInfo jobInfo) {
    if (Objects.equals(jobInfo.getMode(), JobInfo.MODE.CRON.name()) && !CronExpression
        .isValidExpression(jobInfo.getCron())) {
      throw Exceptions.fail("定时表达式格式有误");
    }
    if (Objects.equals(jobInfo.getMode(), MODE.SIMPLE.name()) && jobInfo.getTimestamp() == null) {
      throw Exceptions.fail("执行时间不能为空");
    }
    if (Objects.equals(jobInfo.getMode(), JobInfo.MODE.DELAY.name())) {
      if (jobInfo.getDelay() == null || jobInfo.getInitDelay() == null) {
        throw Exceptions.fail("延时格式有误");
      }
    }
  }
}
