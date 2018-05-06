package justlive.earth.breeze.frost.core.job;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import justlive.earth.breeze.frost.api.model.JobExecuteParam;
import justlive.earth.breeze.frost.api.model.JobExecuteRecord;
import justlive.earth.breeze.frost.api.model.JobGroup;
import justlive.earth.breeze.frost.api.model.JobInfo;
import justlive.earth.breeze.frost.api.model.JobRecordStatus;
import justlive.earth.breeze.frost.core.config.JobProperties;
import justlive.earth.breeze.frost.core.dispacher.Dispatcher;
import justlive.earth.breeze.frost.core.notify.Event;
import justlive.earth.breeze.frost.core.notify.EventPublisher;
import justlive.earth.breeze.frost.core.persistence.JobRepository;
import justlive.earth.breeze.frost.core.util.SpringBeansHolder;
import justlive.earth.breeze.snow.common.base.exception.CodedException;
import justlive.earth.breeze.snow.common.base.util.Checks;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * job分发包装
 * 
 * @author wubo
 *
 */
@NoArgsConstructor
@RequiredArgsConstructor
public class JobDispatchWrapper extends AbstractWrapper {

  /**
   * id for job
   */
  @NonNull
  private String id;

  private String loggerId;

  private JobRecordStatus jobRecordStatus;

  private boolean failRetry;

  private JobExecuteParam param;

  public JobDispatchWrapper(String id, String loggerId) {
    this.id = id;
    this.loggerId = loggerId;
  }

  @Override
  public void doRun() {
    Date time = Date.from(ZonedDateTime.now().toInstant());
    param = new JobExecuteParam(id);
    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    JobInfo jobInfo = jobRepository.findJobInfoById(id);
    JobLogger jobLogger = SpringBeansHolder.getBean(JobLogger.class);
    if (loggerId == null) {
      loggerId = jobLogger.bindLog(id);
      JobExecuteRecord record = this.record(id, loggerId);
      jobRepository.addJobRecord(record);
    } else {
      failRetry = true;
    }
    jobRecordStatus = this.recordStatus(loggerId);
    jobRecordStatus.setTime(time);
    if (failRetry) {
      jobRecordStatus.setType(2);
    } else {
      jobRecordStatus.setType(0);
    }

    String key;
    if (Objects.equals(JobInfo.TYPE.SCRIPT.name(), jobInfo.getType())) {
      if (jobInfo.getGroup() != null && jobInfo.getGroup().getGroupKey() != null) {
        key = String.join(JobProperties.SEPERATOR, JobProperties.JOB_SCRIPT_PREFIX,
            jobInfo.getGroup().getGroupKey());
      } else {
        key = JobProperties.JOB_SCRIPT_PREFIX;
      }
    } else {
      JobGroup jobGroup = Checks.notNull(Checks.notNull(jobInfo).getGroup());
      key = String.join(JobProperties.SEPERATOR, JobProperties.JOB_GROUP_PREFIX,
          jobGroup.getGroupKey(), jobGroup.getJobKey());
      param.setHandlerId(jobInfo.getGroup().getJobKey());
    }
    param.setTopicKey(key);
    param.setLoggerId(loggerId);
    param.setFailRetry(failRetry);
    param.setType(jobInfo.getType());
    param.setFailStrategy(jobInfo.getFailStrategy());

    Dispatcher dispatcher = SpringBeansHolder.getBean(Dispatcher.class);
    dispatcher.dispatch(param);

  }

  @Override
  public void success() {
    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    jobRecordStatus.setStatus(JobExecuteRecord.STATUS.SUCCESS.name());
    jobRecordStatus.setMsg("调度成功");
    jobRepository.addJobRecordStatus(jobRecordStatus);
  }

  @Override
  public void exception(Exception e) {
    super.exception(e);

    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    jobRecordStatus.setStatus(JobExecuteRecord.STATUS.FAIL.name());
    if (e instanceof CodedException) {
      jobRecordStatus.setMsg(((CodedException) e).getErrorCode().toString());
    } else {
      jobRecordStatus.setMsg(e.getMessage());
    }
    jobRepository.addJobRecordStatus(jobRecordStatus);

    EventPublisher publisher = SpringBeansHolder.getBean(EventPublisher.class);
    publisher.publish(new Event(param, Event.TYPE.DISPATCH_FAIL.name(), jobRecordStatus.getMsg(),
        jobRecordStatus.getTime().getTime()));

    if (!failRetry) {
      param.setLoggerId(loggerId);
      param.setFailRetry(failRetry);
      publisher.publish(new Event(param, Event.TYPE.DISPATCH_FAIL_RETRY.name(),
          jobRecordStatus.getMsg(), jobRecordStatus.getTime().getTime()));
    }

  }
}
