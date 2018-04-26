package justlive.earth.breeze.frost.core.job;

import java.time.ZonedDateTime;
import java.util.Date;
import justlive.earth.breeze.frost.api.model.JobExecuteRecord;
import justlive.earth.breeze.frost.api.model.JobInfo;
import justlive.earth.breeze.frost.core.persistence.JobRepository;
import justlive.earth.breeze.frost.core.util.SpringBeansHolder;
import justlive.earth.breeze.snow.common.base.exception.CodedException;

/**
 * 执行job抽象
 * 
 * @author wubo
 *
 */
public abstract class AbstractJobExecuteWrapper extends AbstractWrapper {

  protected JobInfo jobInfo;

  protected JobExecuteRecord jobRecord;

  protected void before() {
    JobRepository jobRepository = SpringBeansHolder.getBean(JobRepository.class);
    jobRecord = jobRepository.findJobExecuteRecordById(jobInfo.getLogId());
    if (jobRecord == null) {
      jobRecord = this.record(jobInfo.getId());
    }
    jobRecord.setExecuteTime(Date.from(ZonedDateTime.now().toInstant()));
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
