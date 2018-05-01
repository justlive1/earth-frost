package justlive.earth.breeze.frost.executor.redis.service;

import java.util.Collections;
import java.util.List;
import org.redisson.executor.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import justlive.earth.breeze.frost.api.model.JobExecuteRecord;
import justlive.earth.breeze.frost.api.model.JobExecutor;
import justlive.earth.breeze.frost.api.model.JobInfo;
import justlive.earth.breeze.frost.api.model.JobScript;
import justlive.earth.breeze.frost.api.model.Page;
import justlive.earth.breeze.frost.core.job.JobLogger;
import justlive.earth.breeze.frost.core.job.JobSchedule;
import justlive.earth.breeze.frost.core.persistence.JobRepository;
import justlive.earth.breeze.frost.core.service.JobService;
import justlive.earth.breeze.snow.common.base.exception.Exceptions;

/**
 * redis调度服务实现类
 * 
 * @author wubo
 *
 */
@Service
public class RedisJobServiceImpl implements JobService {

  @Autowired
  JobRepository jobRepository;

  @Autowired(required = false)
  JobSchedule jobSchedule;

  @Autowired
  JobLogger jobLogger;

  @Override
  public int countExecutors() {
    return jobRepository.countExecutors();
  }

  @Override
  public List<JobExecutor> queryExecutors() {
    return jobRepository.queryJobExecutors();
  }

  @Override
  public String addJob(JobInfo jobInfo) {

    if (!CronExpression.isValidExpression(jobInfo.getCron())) {
      throw Exceptions.fail("300001", "定时表达式格式有误");
    }
    if (jobInfo.isAuto()) {
      jobInfo.setStatus(JobInfo.STATUS.NORMAL.name());
    } else {
      jobInfo.setStatus(JobInfo.STATUS.PAUSED.name());
    }
    jobRepository.addJob(jobInfo);
    if (jobInfo.isAuto()) {
      jobSchedule.addJob(jobInfo.getId(), jobInfo.getCron());
    }

    return jobInfo.getId();
  }

  @Override
  public void updateJob(JobInfo jobInfo) {

    if (!CronExpression.isValidExpression(jobInfo.getCron())) {
      throw Exceptions.fail("300001", "定时表达式格式有误");
    }

    JobInfo localJobInfo = jobRepository.findJobInfoById(jobInfo.getId());
    if (localJobInfo == null) {
      throw Exceptions.fail("300002", "未查询到Job记录");
    }

    localJobInfo.setCron(jobInfo.getCron());
    localJobInfo.setName(jobInfo.getName());
    localJobInfo.setGroup(jobInfo.getGroup());
    localJobInfo.setParam(jobInfo.getParam());
    localJobInfo.setType(jobInfo.getType());
    localJobInfo.setScript(jobInfo.getScript());

    jobRepository.updateJob(localJobInfo);

    if (JobInfo.STATUS.NORMAL.name().equals(localJobInfo.getStatus())) {
      jobSchedule.refreshJob(jobInfo.getId(), jobInfo.getCron());
    }
  }

  @Override
  public void pauseJob(String jobId) {

    JobInfo localJobInfo = jobRepository.findJobInfoById(jobId);
    if (localJobInfo == null) {
      throw Exceptions.fail("300002", "未查询到Job记录");
    }

    localJobInfo.setStatus(JobInfo.STATUS.PAUSED.name());
    localJobInfo.setScript(null);
    jobSchedule.pauseJob(jobId);
    jobRepository.updateJob(localJobInfo);
  }

  @Override
  public void resumeJob(String jobId) {
    jobSchedule.resumeJob(jobId);
    JobInfo jobInfo = jobRepository.findJobInfoById(jobId);
    jobInfo.setStatus(JobInfo.STATUS.NORMAL.name());
    jobInfo.setScript(null);
    jobRepository.updateJob(jobInfo);
  }

  @Override
  public void removeJob(String jobId) {
    jobSchedule.removeJob(jobId);
    jobRepository.removeJobRecords(jobId);
    jobRepository.removeJob(jobId);
    jobLogger.removeLogger(jobId);
  }

  @Override
  public void triggerJob(String jobId) {
    jobSchedule.triggerJob(jobId);
  }

  @Override
  public int countJobInfos() {
    return jobRepository.countJobInfos();
  }

  @Override
  public Page<JobInfo> queryJobInfos(int pageIndex, int pageSize) {
    Page<JobInfo> page = new Page<>();
    page.setPageIndex(pageIndex);
    page.setPageSize(pageSize);

    int totalCount = this.countJobInfos();
    page.setTotalCount(totalCount);

    if (totalCount == 0) {
      return page;
    }
    // 倒序
    int from = Math.max(totalCount - page.getTo(), 0);
    int to = totalCount - page.getFrom();

    List<JobInfo> list = jobRepository.queryJobInfos(from, to);
    Collections.reverse(list);
    page.setItems(list);

    return page;
  }

  @Override
  public List<JobInfo> queryAllJobs() {
    return jobRepository.queryAllJobs();
  }

  @Override
  public JobInfo findJobInfoById(String id) {
    return jobRepository.findJobInfoById(id);
  }

  @Override
  public String addJobRecord(JobExecuteRecord record) {
    return jobRepository.addJobRecord(record);
  }

  @Override
  public Page<JobExecuteRecord> queryJobRecords(String groupKey, String jobKey, String jobId,
      int pageIndex, int pageSize) {
    Page<JobExecuteRecord> page = new Page<>();
    page.setPageIndex(pageIndex);
    page.setPageSize(pageSize);

    int totalCount = jobRepository.countJobRecords(groupKey, jobKey, jobId);
    page.setTotalCount(totalCount);

    if (totalCount == 0) {
      return page;
    }
    // 倒序
    int from = Math.max(totalCount - page.getTo(), 0);
    int to = totalCount - page.getFrom();
    List<JobExecuteRecord> list = jobRepository.queryJobRecords(groupKey, jobKey, jobId, from, to);
    Collections.reverse(list);
    page.setItems(list);
    return page;
  }

  @Override
  public void addJobScript(JobScript script) {
    jobRepository.addJobScript(script);
  }

  @Override
  public List<JobScript> queryJobScripts(String jobId) {
    return jobRepository.queryJobScripts(jobId);
  }

}
