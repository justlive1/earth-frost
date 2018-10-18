package vip.justlive.frost.core.job;

import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.core.config.JobConfig;

/**
 * bean模式job包装
 * 
 * @author wubo
 *
 */
public class JobBeanExecuteWrapper extends AbstractJobExecuteWrapper {

  public JobBeanExecuteWrapper() {}

  public JobBeanExecuteWrapper(JobExecuteParam jobExecuteParam) {
    this.jobExecuteParam = jobExecuteParam;
  }

  @Override
  public void doRun() {
    this.before();
    BaseJob job = getIJob();
    job.execute(new DefaultJobContext(jobInfo, jobExecuteParam));
  }

  @Override
  protected BaseJob getIJob() {
    return JobConfig.findJob(jobExecuteParam.getHandlerId());
  }

}
