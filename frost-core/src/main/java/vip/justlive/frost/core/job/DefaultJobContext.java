package vip.justlive.frost.core.job;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobSharding;

/**
 * 默认Job上下文
 * 
 * @author wubo
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class DefaultJobContext implements JobContext {

  private JobInfo jobInfo;

  private JobExecuteParam jobParam;

  @Override
  public JobInfo getInfo() {
    return jobInfo;
  }

  @Override
  public String getParam() {
    return jobInfo.getParam();
  }

  @Override
  public JobSharding getSharding() {
    return jobParam.getSharding();
  }
}
