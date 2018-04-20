package justlive.earth.breeze.frost.executor.redis.job;

import java.time.ZonedDateTime;
import java.util.Date;
import justlive.earth.breeze.frost.core.job.AbstractWrapper;
import justlive.earth.breeze.frost.core.job.DefaultJobContext;
import justlive.earth.breeze.frost.core.job.IJob;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.persistence.JobRepository;
import justlive.earth.breeze.frost.core.util.SpringBeansHolder;
import justlive.earth.breeze.snow.common.base.exception.CodedException;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * job包装
 * 
 * @author wubo
 *
 */

@NoArgsConstructor
@RequiredArgsConstructor
public class JobExecuteWrapper extends AbstractWrapper {

  @NonNull
  private JobInfo jobInfo;

  private JobExecuteRecord jobRecord;

  @Override
  public void doRun() {

    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    jobRecord = jobRepository.findJobExecuteRecordById(jobInfo.getLogId());
    if (jobRecord == null) {
      jobRecord = this.record(jobInfo.getId());
    }
    jobRecord.setExecuteTime(Date.from(ZonedDateTime.now().toInstant()));
    IJob job = SpringBeansHolder.getBean(jobInfo.getGroup().getJobKey(), IJob.class);
    job.execute(new DefaultJobContext(jobInfo));
  }

  @Override
  public void success() {
    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    jobRecord.setExecuteStatus(JobExecuteRecord.STATUS.SUCCESS.name());
    jobRecord.setExecuteMsg("执行成功");
    jobRepository.updateJobRecord(jobRecord);
  }

  @Override
  public void exception(Exception e) {
    super.exception(e);

    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);

    jobRecord.setExecuteStatus(JobExecuteRecord.STATUS.FAIL.name());
    if (e instanceof CodedException) {
      jobRecord.setExecuteMsg(((CodedException) e).getErrorCode().toString());
    } else {
      jobRecord.setExecuteMsg(e.getMessage());
    }

    jobRepository.updateJobRecord(jobRecord);
  }
}
