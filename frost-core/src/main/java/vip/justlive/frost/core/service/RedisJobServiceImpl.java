package vip.justlive.frost.core.service;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.redisson.api.RedissonClient;
import org.redisson.executor.CronExpression;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobScript;
import vip.justlive.frost.api.model.JobStatictis;
import vip.justlive.frost.api.model.Page;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.frost.core.job.JobLogger;
import vip.justlive.frost.core.job.JobSchedule;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.ioc.Bean;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * redis调度服务实现类
 *
 * @author wubo
 */
@Bean
public class RedisJobServiceImpl implements JobService {

  @Override
  public int countExecutors() {
    return BeanStore.getBean(JobRepository.class).countExecutors();
  }

  @Override
  public List<JobExecutor> queryExecutors() {
    return BeanStore.getBean(JobRepository.class).queryJobExecutors();
  }

  @Override
  public String addJob(JobInfo jobInfo) {

    if (Objects.equals(jobInfo.getMode(), JobInfo.MODE.CRON.name())
        && !CronExpression.isValidExpression(jobInfo.getCron())) {
      throw Exceptions.fail("300001", "定时表达式格式有误");
    }
    if (jobInfo.isAuto()) {
      jobInfo.setStatus(JobInfo.STATUS.NORMAL.name());
    }
    if (jobInfo.getStatus() == null || jobInfo.getStatus().length() == 0) {
      jobInfo.setStatus(JobInfo.STATUS.PAUSED.name());
    }
    BeanStore.getBean(JobRepository.class).addJob(jobInfo);
    if (jobInfo.isAuto()) {
      BeanStore.getBean(JobSchedule.class).addJob(jobInfo.getId());
    }

    return jobInfo.getId();
  }

  @Override
  public void updateJob(JobInfo jobInfo) {

    if (Objects.equals(jobInfo.getMode(), JobInfo.MODE.CRON.name())
        && !CronExpression.isValidExpression(jobInfo.getCron())) {
      throw Exceptions.fail("300001", "定时表达式格式有误");
    }

    JobInfo localJobInfo = BeanStore.getBean(JobRepository.class).findJobInfoById(jobInfo.getId());
    if (localJobInfo == null) {
      throw Exceptions.fail("300002", "未查询到Job记录");
    }

    if (jobInfo.getChildJobIds() != null
        && Arrays.asList(jobInfo.getChildJobIds()).contains(jobInfo.getId())) {
      throw Exceptions.fail("300003", "子任务不能包含本任务");
    }

    this.mergeData(localJobInfo, jobInfo);

    BeanStore.getBean(JobRepository.class).updateJob(localJobInfo);

    if (JobInfo.STATUS.NORMAL.name().equals(localJobInfo.getStatus())) {
      switch (JobInfo.MODE.valueOf(localJobInfo.getMode())) {
        case SIMPLE:
          BeanStore.getBean(JobSchedule.class).refreshJob(jobInfo.getId(), jobInfo.getTimestamp());
          break;
        case DELAY:
          BeanStore.getBean(JobSchedule.class).refreshJob(jobInfo.getId(), jobInfo.getInitDelay(),
              jobInfo.getDelay());
          break;
        case CRON:
          BeanStore.getBean(JobSchedule.class).refreshJob(jobInfo.getId(), jobInfo.getCron());
          break;
        default:
          break;
      }
    }
  }

  @Override
  public void pauseJob(String jobId) {

    JobInfo localJobInfo = BeanStore.getBean(JobRepository.class).findJobInfoById(jobId);
    if (localJobInfo == null) {
      throw Exceptions.fail("300002", "未查询到Job记录");
    }

    localJobInfo.setStatus(JobInfo.STATUS.PAUSED.name());
    localJobInfo.setScript(null);
    BeanStore.getBean(JobSchedule.class).pauseJob(jobId);
    BeanStore.getBean(JobRepository.class).updateJob(localJobInfo);
  }

  @Override
  public void resumeJob(String jobId) {
    BeanStore.getBean(JobSchedule.class).resumeJob(jobId);
    JobInfo jobInfo = BeanStore.getBean(JobRepository.class).findJobInfoById(jobId);
    jobInfo.setStatus(JobInfo.STATUS.NORMAL.name());
    jobInfo.setScript(null);
    BeanStore.getBean(JobRepository.class).updateJob(jobInfo);
  }

  @Override
  public void removeJob(String jobId) {
    BeanStore.getBean(JobSchedule.class).removeJob(jobId);
    BeanStore.getBean(JobRepository.class).removeJobRecords(jobId);
    BeanStore.getBean(JobRepository.class).removeJobScripts(jobId);
    BeanStore.getBean(JobRepository.class).removeJob(jobId);
    BeanStore.getBean(JobLogger.class).removeLogger(jobId);
  }

  @Override
  public void triggerJob(String jobId) {
    BeanStore.getBean(JobSchedule.class).triggerJob(jobId);
  }

  @Override
  public int countJobInfos() {
    return BeanStore.getBean(JobRepository.class).countJobInfos();
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

    List<JobInfo> list = BeanStore.getBean(JobRepository.class).queryJobInfos(from, to);
    Collections.reverse(list);
    page.setItems(list);

    return page;
  }

  @Override
  public List<JobInfo> queryAllJobs() {
    return BeanStore.getBean(JobRepository.class).queryAllJobs();
  }

  @Override
  public JobInfo findJobInfoById(String id) {
    return BeanStore.getBean(JobRepository.class).findJobInfoById(id);
  }

  @Override
  public String addJobRecord(JobExecuteRecord record) {
    return BeanStore.getBean(JobRepository.class).addJobRecord(record);
  }

  @Override
  public Page<JobExecuteRecord> queryJobRecords(String groupKey, String jobKey, String jobId,
      int pageIndex, int pageSize) {
    Page<JobExecuteRecord> page = new Page<>();
    page.setPageIndex(pageIndex);
    page.setPageSize(pageSize);

    int totalCount =
        BeanStore.getBean(JobRepository.class).countJobRecords(groupKey, jobKey, jobId);
    page.setTotalCount(totalCount);

    if (totalCount == 0) {
      return page;
    }
    // 倒序
    int from = Math.max(totalCount - page.getTo(), 0);
    int to = totalCount - page.getFrom();
    List<JobExecuteRecord> list =
        BeanStore.getBean(JobRepository.class).queryJobRecords(groupKey, jobKey, jobId, from, to);
    Collections.reverse(list);
    page.setItems(list);
    return page;
  }

  @Override
  public void addJobScript(JobScript script) {
    BeanStore.getBean(JobRepository.class).addJobScript(script);
  }

  @Override
  public List<JobScript> queryJobScripts(String jobId) {
    return BeanStore.getBean(JobRepository.class).queryJobScripts(jobId);
  }

  @Override
  public JobStatictis queryJobStatictis(String begin, String end) {
    JobStatictis statictis = new JobStatictis();
    statictis.setTotalJobs((long) this.countJobInfos());
    statictis.setTotalExecutors((long) this.countExecutors());
    statictis.setTotalDispatches(BeanStore.getBean(RedissonClient.class)
        .getAtomicLong(String.format(JobConfig.STAT_TOTAL_TYPE, JobConfig.STAT_TYPE_DISPATCH))
        .get());
    statictis.setTotalRunningExecutions(
        BeanStore.getBean(RedissonClient.class).getAtomicLong(JobConfig.STAT_TOTAL_RUNNING).get());
    List<String> statictisDays = queryStatictisDays(begin, end);
    statictis.setStatictisDays(statictisDays);
    statictis.setFailDispatches(Lists.newArrayList());
    statictis.setSuccessDispatches(Lists.newArrayList());
    statictis.setFailExecutions(Lists.newArrayList());
    statictis.setSuccessExecutions(Lists.newArrayList());
    for (String day : statictisDays) {
      statictis.getSuccessDispatches().add(BeanStore.getBean(RedissonClient.class).getAtomicLong(
          String.format(JobConfig.STAT_DATE_TYPE_SUCCESS, day, JobConfig.STAT_TYPE_DISPATCH))
          .get());
      statictis.getFailDispatches().add(BeanStore.getBean(RedissonClient.class).getAtomicLong(
          String.format(JobConfig.STAT_DATE_TYPE_FAIL, day, JobConfig.STAT_TYPE_DISPATCH)).get());
      statictis.getSuccessExecutions().add(BeanStore.getBean(RedissonClient.class).getAtomicLong(
          String.format(JobConfig.STAT_DATE_TYPE_SUCCESS, day, JobConfig.STAT_TYPE_EXECUTE)).get());
      statictis.getFailExecutions().add(BeanStore.getBean(RedissonClient.class).getAtomicLong(
          String.format(JobConfig.STAT_DATE_TYPE_FAIL, day, JobConfig.STAT_TYPE_EXECUTE)).get());
    }

    return statictis;
  }

  @Override
  public void removeJobRecords(String jobId) {
    BeanStore.getBean(JobRepository.class).removeJobRecords(jobId);
  }

  private List<String> queryStatictisDays(String begin, String end) {
    List<String> statictisDays = Lists.newArrayList();

    LocalDate from = LocalDate.parse(begin);
    LocalDate to = LocalDate.parse(end);

    while (from.isBefore(to) || from.equals(to)) {
      statictisDays.add(DateTimeFormatter.ISO_LOCAL_DATE.format(from));
      from = from.plusDays(1);
    }

    return statictisDays;
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
