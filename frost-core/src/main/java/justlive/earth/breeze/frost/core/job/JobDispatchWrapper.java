package justlive.earth.breeze.frost.core.job;

import java.time.ZonedDateTime;
import java.util.Date;
import justlive.earth.breeze.frost.api.model.JobExecuteRecord;
import justlive.earth.breeze.frost.api.model.JobInfo;
import justlive.earth.breeze.frost.core.dispacher.Dispatcher;
import justlive.earth.breeze.frost.core.persistence.JobRepository;
import justlive.earth.breeze.frost.core.util.SpringBeansHolder;
import justlive.earth.breeze.snow.common.base.exception.CodedException;
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

  private JobExecuteRecord record;

  @Override
  public void doRun() {

    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    JobInfo job = jobRepository.findJobInfoById(id);
    JobLogger jobLogger = SpringBeansHolder.getBean(JobLogger.class);
    String loggerId = jobLogger.bindLog(id);
    record = this.record(id, loggerId);
    record.setDispachTime(Date.from(ZonedDateTime.now().toInstant()));
    jobRepository.addJobRecord(record);
    Dispatcher dispatcher = SpringBeansHolder.getBean(Dispatcher.class);
    dispatcher.dispatch(job);

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
  }
}
