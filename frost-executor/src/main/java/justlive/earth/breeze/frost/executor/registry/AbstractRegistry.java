package justlive.earth.breeze.frost.executor.registry;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import justlive.earth.breeze.frost.core.job.IJob;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.registry.Registry;
import justlive.earth.breeze.frost.core.util.IpUtils;
import justlive.earth.breeze.frost.executor.config.ExecutorProperties;

/**
 * 注册抽象类
 * 
 * @author wubo
 *
 */
public abstract class AbstractRegistry implements Registry {

  @Autowired
  protected ExecutorProperties executorProps;

  @Autowired
  protected List<IJob> jobs;

  /**
   * 当前执行器
   * 
   * @return
   */
  protected JobExecutor jobExecutor() {
    JobExecutor jobExecutor = new JobExecutor();
    jobExecutor.setId(UUID.randomUUID().toString());
    jobExecutor.setName(executorProps.getName());
    jobExecutor.setKey(executorProps.getKey());

    String address = executorProps.getIp();
    if (!StringUtils.hasText(address)) {
      address = IpUtils.ip();
    }
    address += IpUtils.SEPERATOR + executorProps.getPort();
    jobExecutor.setAddress(address);
    return jobExecutor;
  }

}
