package vip.justlive.frost.core.job;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobRecordStatus;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.frost.core.monitor.Monitor;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.notify.EventPublisher;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.frost.core.util.IpUtils;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * 执行job抽象
 * 
 * @author wubo
 *
 */
public abstract class AbstractJobExecuteWrapper extends AbstractWrapper {

  protected JobExecuteParam jobExecuteParam;

  protected JobInfo jobInfo;

  protected JobRecordStatus jobRecordStatus;

  protected boolean success;

  protected void before() {
    Instant instant = ZonedDateTime.now().toInstant();
    // 触发开始执行任务事件
    EventPublisher publisher = BeanStore.getBean(EventPublisher.class);
    publisher.publish(
        new Event(jobExecuteParam, Event.TYPE.EXECUTE_ENTER.name(), null, instant.toEpochMilli()));
    JobLogger jobLogger = BeanStore.getBean(JobLogger.class);
    jobLogger.enter(jobExecuteParam.getLoggerId(), JobConfig.CENTER_STATISTICS_EXECUTE);
    JobRepository jobRepository = BeanStore.getBean(JobRepository.class);
    jobInfo = jobRepository.findJobInfoById(jobExecuteParam.getJobId());
    jobRecordStatus = new JobRecordStatus();
    jobRecordStatus.setId(UUID.randomUUID().toString());
    if (jobExecuteParam.isFailRetry()) {
      jobRecordStatus.setType(3);
    } else {
      jobRecordStatus.setType(1);
    }
    jobRecordStatus.setLoggerId(jobExecuteParam.getLoggerId());
    jobRecordStatus.setTime(Date.from(instant));

    Monitor monitor = BeanStore.getBean(Monitor.class);
    monitor.watch(jobExecuteParam);
  }

  @Override
  public void success() {
    success = true;
    long end = ZonedDateTime.now().toInstant().toEpochMilli();
    JobRepository jobRepository = BeanStore.getBean(JobRepository.class);
    jobRecordStatus.setStatus(JobExecuteRecord.STATUS.SUCCESS.name());
    String msg = String.format("执行成功 [%s]", address());
    if (jobExecuteParam.getSharding() != null) {
      msg += String.format("[%s of %s]", jobExecuteParam.getSharding().getIndex(),
          jobExecuteParam.getSharding().getTotal());
    }
    jobRecordStatus.setMsg(msg);
    jobRecordStatus.setDuration(end - jobRecordStatus.getTime().getTime());
    jobRepository.addJobRecordStatus(jobRecordStatus);
    // 触发任务执行成功事件
    EventPublisher publisher = BeanStore.getBean(EventPublisher.class);
    publisher.publish(new Event(jobExecuteParam, Event.TYPE.EXECUTE_SUCCESS.name(), null, end));
  }

  @Override
  public void exception(Exception e) {
    success = false;
    super.exception(e);
    jobRecordStatus.setStatus(JobExecuteRecord.STATUS.FAIL.name());
    String cause;
    if (e instanceof CodedException) {
      cause = ((CodedException) e).getErrorCode().toString();
    } else {
      cause = e.getMessage();
    }

    if (jobExecuteParam.getSharding() != null) {
      jobRecordStatus.setMsg(String.format("执行失败 [%s] [%s of %s] [%s]", address(),
          jobExecuteParam.getSharding().getIndex(), jobExecuteParam.getSharding().getTotal(),
          cause));
    } else {
      jobRecordStatus.setMsg(String.format("执行失败 [%s] [%s]", address(), cause));
    }
    jobRecordStatus.setDuration(
        ZonedDateTime.now().toInstant().toEpochMilli() - jobRecordStatus.getTime().getTime());
    JobRepository jobRepository = BeanStore.getBean(JobRepository.class);
    jobRepository.addJobRecordStatus(jobRecordStatus);

    BaseJob job = getIJob();
    if (job.exception()) {
      // 通知事件
      EventPublisher publisher = BeanStore.getBean(EventPublisher.class);
      publisher.publish(new Event(jobExecuteParam, Event.TYPE.EXECUTE_FAIL.name(),
          jobRecordStatus.getMsg(), jobRecordStatus.getTime().getTime()));
      if (!jobExecuteParam.isFailRetry()) {
        // 失败重试事件
        jobExecuteParam.setFailRetry(true);
        publisher.publish(new Event(jobExecuteParam, Event.TYPE.EXECUTE_FAIL_RETRY.name(),
            jobRecordStatus.getMsg(), jobRecordStatus.getTime().getTime()));
      }
    }
  }

  @Override
  public void finshed() {
    Monitor monitor = BeanStore.getBean(Monitor.class);
    monitor.unWatch(jobExecuteParam);
    JobLogger jobLogger = BeanStore.getBean(JobLogger.class);
    jobLogger.leave(jobExecuteParam.getLoggerId(), JobConfig.CENTER_STATISTICS_EXECUTE, success);
  }

  /**
   * 获取任务处理逻辑
   * 
   * @return
   */
  protected abstract BaseJob getIJob();

  private String address() {
    String address = JobConfig.getExecutor().getIp();
    if (address == null || address.length() == 0) {
      address = IpUtils.ip();
    }
    address += IpUtils.SEPERATOR + JobConfig.getExecutor().getPort();
    return address;
  }
}
