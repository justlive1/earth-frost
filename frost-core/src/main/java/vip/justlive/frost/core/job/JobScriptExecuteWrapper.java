package vip.justlive.frost.core.job;

import java.util.Objects;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.util.ScriptJobFactory;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * script模式job包装
 * 
 * @author wubo
 *
 */
public class JobScriptExecuteWrapper extends AbstractJobExecuteWrapper {

  public JobScriptExecuteWrapper() {}

  public JobScriptExecuteWrapper(JobExecuteParam jobExecuteParam) {
    this.jobExecuteParam = jobExecuteParam;
  }

  @Override
  public void doRun() {
    this.before();
    if (!Objects.equals(jobInfo.getType(), JobInfo.TYPE.SCRIPT.name())) {
      throw Exceptions.fail("30002", "执行job类型不匹配");
    }
    BaseJob job = getIJob();
    job.execute(new DefaultJobContext(jobInfo, jobExecuteParam));
  }

  @Override
  protected BaseJob getIJob() {
    return ScriptJobFactory.parse(jobInfo.getScript());
  }

}
