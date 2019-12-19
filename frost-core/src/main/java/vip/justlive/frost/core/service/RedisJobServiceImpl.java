package vip.justlive.frost.core.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobScript;
import vip.justlive.frost.api.model.JobStatistic;
import vip.justlive.frost.api.model.Page;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.job.JobLogger;
import vip.justlive.frost.core.job.JobSchedule;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * redis调度服务实现类
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class RedisJobServiceImpl implements JobService {

  private final JobRepository jobRepository;
  private final JobSchedule jobSchedule;
  private final JobLogger jobLogger;

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
    if (jobInfo.isAuto()) {
      jobInfo.setStatus(JobInfo.STATUS.NORMAL.name());
    }
    if (jobInfo.getStatus() == null || jobInfo.getStatus().length() == 0) {
      jobInfo.setStatus(JobInfo.STATUS.PAUSED.name());
    }
    jobRepository.addJob(jobInfo);
    if (jobInfo.isAuto()) {
      jobSchedule.addJob(jobInfo.getId());
    }

    return jobInfo.getId();
  }

  @Override
  public void updateJob(JobInfo jobInfo) {
    JobInfo localJobInfo = jobRepository.findJobInfoById(jobInfo.getId());
    if (localJobInfo == null) {
      throw Exceptions.fail("未查询到Job记录");
    }
    this.mergeData(localJobInfo, jobInfo);

    jobRepository.updateJob(localJobInfo);

    if (JobInfo.STATUS.NORMAL.name().equals(localJobInfo.getStatus())) {
      switch (JobInfo.MODE.valueOf(localJobInfo.getMode())) {
        case SIMPLE:
          jobSchedule.refreshJob(jobInfo.getId(), jobInfo.getTimestamp());
          break;
        case DELAY:
          jobSchedule.refreshJob(jobInfo.getId(), jobInfo.getInitDelay(), jobInfo.getDelay());
          break;
        case CRON:
          jobSchedule.refreshJob(jobInfo.getId(), jobInfo.getCron());
          break;
        default:
          break;
      }
    }
  }

  @Override
  public void pauseJob(String jobId) {

    JobInfo localJobInfo = jobRepository.findJobInfoById(jobId);
    if (localJobInfo == null) {
      throw Exceptions.fail("未查询到Job记录");
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
    jobRepository.removeJobScripts(jobId);
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

  @Override
  public JobStatistic queryJobStatistic(String begin, String end) {
    JobStatistic statistic = new JobStatistic();
    statistic.setTotalJobs((long) this.countJobInfos());
    statistic.setTotalExecutors((long) this.countExecutors());
    statistic.setTotalDispatches(jobRepository
        .countByType(String.format(Container.STAT_TOTAL_TYPE, Container.STAT_TYPE_DISPATCH)));
    statistic.setTotalRunningExecutions(jobRepository.countByType(Container.STAT_TOTAL_RUNNING));
    List<String> statisticDays = queryStatisticDays(begin, end);
    statistic.setStatisticDays(statisticDays);
    statistic.setFailDispatches(new ArrayList<>());
    statistic.setSuccessDispatches(new ArrayList<>());
    statistic.setFailExecutions(new ArrayList<>());
    statistic.setSuccessExecutions(new ArrayList<>());
    for (String day : statisticDays) {
      statistic.getSuccessDispatches().add(jobRepository.countByType(
          String.format(Container.STAT_DATE_TYPE_SUCCESS, day, Container.STAT_TYPE_DISPATCH)));
      statistic.getFailDispatches().add(jobRepository.countByType(
          String.format(Container.STAT_DATE_TYPE_FAIL, day, Container.STAT_TYPE_DISPATCH)));
      statistic.getSuccessExecutions().add(jobRepository.countByType(
          String.format(Container.STAT_DATE_TYPE_SUCCESS, day, Container.STAT_TYPE_EXECUTE)));
      statistic.getFailExecutions().add(jobRepository.countByType(
          String.format(Container.STAT_DATE_TYPE_FAIL, day, Container.STAT_TYPE_EXECUTE)));
    }

    return statistic;
  }

  @Override
  public void removeJobRecords(String jobId) {
    jobRepository.removeJobRecords(jobId);
  }

  private List<String> queryStatisticDays(String begin, String end) {
    List<String> statisticDays = new ArrayList<>();

    LocalDate from = LocalDate.parse(begin);
    LocalDate to = LocalDate.parse(end);

    while (from.isBefore(to) || from.equals(to)) {
      statisticDays.add(DateTimeFormatter.ISO_LOCAL_DATE.format(from));
      from = from.plusDays(1);
    }

    return statisticDays;
  }

  private void mergeData(JobInfo localJobInfo, JobInfo jobInfo) {
    localJobInfo.setMode(jobInfo.getMode());
    localJobInfo.setTimestamp(jobInfo.getTimestamp());
    localJobInfo.setInitDelay(jobInfo.getInitDelay());
    localJobInfo.setDelay(jobInfo.getDelay());
    localJobInfo.setCron(jobInfo.getCron());
    localJobInfo.setName(jobInfo.getName());
    localJobInfo.setGroup(jobInfo.getGroup());
    localJobInfo.setParam(jobInfo.getParam());
    localJobInfo.setType(jobInfo.getType());
    localJobInfo.setScript(jobInfo.getScript());
    localJobInfo.setFailStrategy(jobInfo.getFailStrategy());
    localJobInfo.setNotifyMails(jobInfo.getNotifyMails());
    localJobInfo.setChildJobIds(jobInfo.getChildJobIds());
    localJobInfo.setTimeout(jobInfo.getTimeout());
    localJobInfo.setUseSharding(jobInfo.isUseSharding());
    localJobInfo.setSharding(jobInfo.getSharding());
  }
}
