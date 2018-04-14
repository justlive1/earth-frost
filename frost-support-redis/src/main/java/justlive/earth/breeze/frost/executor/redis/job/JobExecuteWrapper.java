package justlive.earth.breeze.frost.executor.redis.job;

import justlive.earth.breeze.frost.core.job.AbstractWrapper;
import justlive.earth.breeze.frost.core.job.DefaultJobContext;
import justlive.earth.breeze.frost.core.job.IJob;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.service.JobService;
import justlive.earth.breeze.frost.core.util.SpringBeansHolder;
import justlive.earth.breeze.snow.common.base.exception.CodedException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * job包装
 * 
 * @author wubo
 *
 */

@NoArgsConstructor
@AllArgsConstructor
public class JobExecuteWrapper extends AbstractWrapper {

  private JobInfo jobInfo;

  @Override
  public void doRun() {

    IJob job = SpringBeansHolder.getBean(jobInfo.getGroup().getJobKey(), IJob.class);
    job.execute(new DefaultJobContext(jobInfo));
  }

  @Override
  public void success() {
    JobService jobService = SpringBeansHolder.getBean(JobService.class);
    JobExecuteRecord record = this.record(jobInfo.getId());
    record.setStatus(JobExecuteRecord.STATUS.EXECUTE_SUCCESS.value());
    record.setMessage(JobExecuteRecord.STATUS.EXECUTE_SUCCESS.msg());
    jobService.addJobRecord(record);
  }

  @Override
  public void exception(Exception e) {
    super.exception(e);

    JobService jobService = SpringBeansHolder.getBean(JobService.class);

    JobExecuteRecord record = this.record(jobInfo.getId());
    record.setStatus(JobExecuteRecord.STATUS.FAIL.value());
    if (e instanceof CodedException) {
      record.setMessage(((CodedException) e).getErrorCode().toString());
    } else {
      record.setMessage(e.getMessage());
    }

    jobService.addJobRecord(record);
  }
}
