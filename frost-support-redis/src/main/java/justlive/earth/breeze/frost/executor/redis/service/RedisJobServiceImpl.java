package justlive.earth.breeze.frost.executor.redis.service;

import java.util.List;
import org.redisson.executor.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import justlive.earth.breeze.frost.core.job.JobSchedule;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.model.Page;
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
    jobInfo.setStatus(JobInfo.STATUS.NORMAL.name());
    jobRepository.addJob(jobInfo);

    String taskId = jobSchedule.addJob(jobInfo.getId(), jobInfo.getCron());
    jobInfo.setTaskId(taskId);
    jobRepository.updateJob(jobInfo);

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

    jobRepository.updateJob(localJobInfo);

    if (JobInfo.STATUS.NORMAL.name().equals(localJobInfo.getStatus())) {
      String taskId = jobSchedule.refreshJob(jobInfo.getId(), jobInfo.getCron());
      localJobInfo.setTaskId(taskId);
      jobRepository.updateJob(localJobInfo);
    }
  }

  @Override
  public void pauseJob(String jobId) {

    JobInfo localJobInfo = jobRepository.findJobInfoById(jobId);
    if (localJobInfo == null) {
      throw Exceptions.fail("300002", "未查询到Job记录");
    }

    localJobInfo.setStatus(JobInfo.STATUS.PAUSED.name());
    jobRepository.updateJob(localJobInfo);

    jobSchedule.pauseJob(jobId);
  }

  @Override
  public void resumeJob(String jobId) {
    String taskId = jobSchedule.resumeJob(jobId);
    JobInfo jobInfo = jobRepository.findJobInfoById(jobId);
    jobInfo.setTaskId(taskId);
    jobInfo.setStatus(JobInfo.STATUS.NORMAL.name());
    jobRepository.updateJob(jobInfo);
  }

  @Override
  public void removeJob(String jobId) {
    jobSchedule.removeJob(jobId);
    jobRepository.removeJobRecords(jobId);
    jobRepository.removeJob(jobId);
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
  public List<JobInfo> queryJobInfos() {
    return jobRepository.queryJobInfos();
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
    List<JobExecuteRecord> list =
        jobRepository.queryJobRecords(groupKey, jobKey, jobId, page.getFrom(), page.getTo());
    page.setItems(list);
    return page;
  }

}
