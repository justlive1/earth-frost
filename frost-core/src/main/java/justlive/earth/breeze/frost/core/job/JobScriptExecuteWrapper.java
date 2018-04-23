package justlive.earth.breeze.frost.core.job;

import java.util.Objects;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.util.ScriptJobFactory;
import justlive.earth.breeze.snow.common.base.exception.Exceptions;

/**
 * script模式job包装
 * 
 * @author wubo
 *
 */
public class JobScriptExecuteWrapper extends AbstractJobExecuteWrapper {

  public JobScriptExecuteWrapper() {}

  public JobScriptExecuteWrapper(JobInfo jobInfo) {
    this.jobInfo = jobInfo;
  }

  @Override
  public void doRun() {
    if (Objects.equals(jobInfo.getType(), JobInfo.TYPE.SCRIPT.name())) {
      throw Exceptions.fail("30002", "执行job类型不匹配");
    }
    this.before();
    IJob job = ScriptJobFactory.parse(jobInfo.getScript());
    job.execute(new DefaultJobContext(jobInfo));
  }

}
