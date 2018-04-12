package justlive.earth.breeze.frost.core.executor;

import justlive.earth.breeze.frost.core.job.DefaultJobContext;
import justlive.earth.breeze.frost.core.job.IJob;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.util.SpringBeansHolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * job包装
 * 
 * @author wubo
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobWrapper implements Runnable {

  private JobInfo jobInfo;

  @Override
  public void run() {

    IJob job = SpringBeansHolder.getBean(jobInfo.getGroup().getJobKey(), IJob.class);
    job.execute(new DefaultJobContext(jobInfo));
  }

}
