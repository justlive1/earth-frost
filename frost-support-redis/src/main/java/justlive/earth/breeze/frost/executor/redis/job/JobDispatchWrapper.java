package justlive.earth.breeze.frost.executor.redis.job;

import justlive.earth.breeze.frost.core.dispacher.Dispatcher;
import justlive.earth.breeze.frost.core.job.AbstractWrapper;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.service.JobService;
import justlive.earth.breeze.frost.core.util.SpringBeansHolder;
import justlive.earth.breeze.snow.common.base.exception.CodedException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * job分发包装
 * 
 * @author wubo
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class JobDispatchWrapper extends AbstractWrapper {

  /**
   * id for job
   */
  private String id;

  @Override
  public void doRun() {

    JobService jobService = SpringBeansHolder.getBean(JobService.class);
    JobInfo job = jobService.findJobInfoById(id);
    Dispatcher dispatcher = SpringBeansHolder.getBean(Dispatcher.class);
    dispatcher.dispatch(job);

  }

  @Override
  public void success() {
    JobService jobService = SpringBeansHolder.getBean(JobService.class);
    JobExecuteRecord record = this.record(id);
    record.setStatus(JobExecuteRecord.STATUS.DISPATCH_SUCCESS.value());
    record.setMessage(JobExecuteRecord.STATUS.DISPATCH_SUCCESS.msg());
    jobService.addJobRecord(record);
  }

  @Override
  public void exception(Exception e) {
    super.exception(e);

    JobService jobService = SpringBeansHolder.getBean(JobService.class);
    JobExecuteRecord record = this.record(id);
    record.setStatus(JobExecuteRecord.STATUS.FAIL.value());
    if (e instanceof CodedException) {
      record.setMessage(((CodedException) e).getErrorCode().toString());
    } else {
      record.setMessage(e.getMessage());
    }

    jobService.addJobRecord(record);
  }
}
