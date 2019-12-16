package vip.justlive.frost.core.job;

import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.core.config.Container;

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
    this.jobExecuteParam.setExecuteAt(time());
  }

  @Override
  public void doRun() {
    this.before();
    BaseJob job = getIJob();
    job.execute(new DefaultJobContext(jobInfo, jobExecuteParam));
  }

  @Override
  protected BaseJob getIJob() {
    return Container.findJob(jobExecuteParam.getHandlerId());
  }

}
