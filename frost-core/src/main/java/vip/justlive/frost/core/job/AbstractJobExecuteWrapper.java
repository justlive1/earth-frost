package vip.justlive.frost.core.job;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobRecordStatus;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.monitor.Monitor;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.notify.EventPublisher;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.exception.WrappedException;

/**
 * 执行job抽象
 *
 * @author wubo
 */
public abstract class AbstractJobExecuteWrapper extends AbstractWrapper {

  protected JobExecuteParam jobExecuteParam;

  protected JobInfo jobInfo;

  protected JobRecordStatus jobRecordStatus;

  protected boolean success;

  protected void before() {
    Instant instant = ZonedDateTime.now().toInstant();
    // 触发开始执行任务事件
    EventPublisher publisher = Container.get().getPublisher();
    publisher.publish(
        new Event(jobExecuteParam, Event.TYPE.EXECUTE_ENTER.name(), null, instant.toEpochMilli()));
    JobLogger jobLogger = Container.get().getJobLogger();
    jobLogger.enter(jobExecuteParam.getLoggerId(), Container.STAT_TYPE_EXECUTE);
    JobRepository jobRepository = Container.get().getJobRepository();
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

    Monitor monitor = Container.get().getMonitor();
    monitor.watch(jobExecuteParam);

    long time = time();
    long misfireThreshold = Container.get().getJobExecutorProperties().getMisfireThreshold();
    if (time - jobExecuteParam.getExecuteAt() > misfireThreshold) {
      throw Exceptions.fail("40000", String.format("任务错过执行，且超过阈值[%s]", misfireThreshold));
    }
  }

  @Override
  public void success() {
    success = true;
    long end = ZonedDateTime.now().toInstant().toEpochMilli();
    JobRepository jobRepository = Container.get().getJobRepository();
    jobRecordStatus.setStatus(JobExecuteRecord.STATUS.SUCCESS.name());
    String msg = String.format("执行成功 [%s]", Container.get().getJobExecutor().getAddress());
    if (jobExecuteParam.getSharding() != null) {
      msg += String.format("[%s of %s]", jobExecuteParam.getSharding().getIndex(),
          jobExecuteParam.getSharding().getTotal());
    }
    jobRecordStatus.setMsg(msg);
    jobRecordStatus.setDuration(end - jobRecordStatus.getTime().getTime());
    jobRepository.addJobRecordStatus(jobRecordStatus);
    // 触发任务执行成功事件
    Container.get().getPublisher()
        .publish(new Event(jobExecuteParam, Event.TYPE.EXECUTE_SUCCESS.name(), null, end));
  }

  @Override
  public void exception(Exception e) {
    success = false;
    super.exception(e);
    jobRecordStatus.setStatus(JobExecuteRecord.STATUS.FAIL.name());
    String cause;
    if (e instanceof WrappedException) {
      e = (Exception) ((WrappedException) e).getException();
    }
    if (e instanceof CodedException) {
      cause = ((CodedException) e).getErrorCode().toString();
    } else {
      cause = e.getMessage();
    }

    if (jobExecuteParam.getSharding() != null) {
      jobRecordStatus.setMsg(String
          .format("执行失败 [%s] [%s of %s] [%s]", Container.get().getJobExecutor().getAddress(),
              jobExecuteParam.getSharding().getIndex(), jobExecuteParam.getSharding().getTotal(),
              cause));
    } else {
      jobRecordStatus.setMsg(
          String.format("执行失败 [%s] [%s]", Container.get().getJobExecutor().getAddress(), cause));
    }
    jobRecordStatus.setDuration(
        ZonedDateTime.now().toInstant().toEpochMilli() - jobRecordStatus.getTime().getTime());
    JobRepository jobRepository = Container.get().getJobRepository();
    jobRepository.addJobRecordStatus(jobRecordStatus);

    BaseJob job = getJob();
    if (job.exception()) {
      // 通知事件
      EventPublisher publisher = Container.get().getPublisher();
      publisher.publish(
          new Event(jobExecuteParam, Event.TYPE.EXECUTE_FAIL.name(), jobRecordStatus.getMsg(),
              jobRecordStatus.getTime().getTime()));
      if (!jobExecuteParam.isFailRetry()) {
        // 失败重试事件
        jobExecuteParam.setFailRetry(true);
        publisher.publish(new Event(jobExecuteParam, Event.TYPE.EXECUTE_FAIL_RETRY.name(),
            jobRecordStatus.getMsg(), jobRecordStatus.getTime().getTime()));
      }
    }
  }

  @Override
  public void finished() {
    Monitor monitor = Container.get().getMonitor();
    monitor.unWatch(jobExecuteParam);
    JobLogger jobLogger = Container.get().getJobLogger();
    jobLogger.leave(jobExecuteParam.getLoggerId(), Container.STAT_TYPE_EXECUTE, success);
  }

  /**
   * 获取任务处理逻辑
   *
   * @return job
   */
  protected abstract BaseJob getJob();

  /**
   * 获取当前时间
   *
   * @return time
   */
  protected long time() {
    return Container.get().getJobRepository().currentTime();
  }

}
