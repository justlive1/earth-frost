package justlive.earth.breeze.frost.core.job;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import justlive.earth.breeze.frost.api.model.JobExecuteParam;
import justlive.earth.breeze.frost.api.model.JobExecuteRecord;
import justlive.earth.breeze.frost.api.model.JobGroup;
import justlive.earth.breeze.frost.api.model.JobInfo;
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

  private JobExecuteRecord record;

  private boolean failRetry;

  public JobDispatchWrapper(String id, String loggerId) {
    this.id = id;
    this.loggerId = loggerId;
  }

  @Override
  public void doRun() {

    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    JobInfo jobInfo = jobRepository.findJobInfoById(id);
    JobLogger jobLogger = SpringBeansHolder.getBean(JobLogger.class);
    if (loggerId == null) {
      loggerId = jobLogger.bindLog(id);
      record = this.record(id, loggerId);
      record.setDispachTime(Date.from(ZonedDateTime.now().toInstant()));
      jobRepository.addJobRecord(record);
    } else {
      record = jobRepository.findJobExecuteRecordById(loggerId);
      failRetry = true;
    }

    JobExecuteParam param = new JobExecuteParam(id);
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

    Dispatcher dispatcher = SpringBeansHolder.getBean(Dispatcher.class);
    dispatcher.dispatch(param);

  }

  @Override
  public void success() {
    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    record.setDispachStatus(JobExecuteRecord.STATUS.SUCCESS.name());
    record.setDispachMsg("调度成功");
    jobRepository.updateJobRecord(record);
  }

  @Override
  public void exception(Exception e) {
    super.exception(e);

    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    record.setDispachStatus(JobExecuteRecord.STATUS.FAIL.name());
    if (e instanceof CodedException) {
      record.setDispachMsg(((CodedException) e).getErrorCode().toString());
    } else {
      record.setDispachMsg(e.getMessage());
    }

    jobRepository.updateJobRecord(record);

    EventPublisher publisher = SpringBeansHolder.getBean(EventPublisher.class);
    publisher.publish(new Event(jobRepository.findJobInfoById(id), Event.TYPE.DISPATCH_FAIL.name(),
        record.getDispachMsg(), record.getDispachTime().getTime()));

    if (!failRetry) {
      JobExecuteParam param = new JobExecuteParam(id);
      param.setLoggerId(loggerId);
      param.setFailRetry(failRetry);
      publisher.publish(new Event(param, Event.TYPE.DISPATCH_FAIL_RETRY.name(),
          record.getDispachMsg(), record.getDispachTime().getTime()));
    }

  }
}
