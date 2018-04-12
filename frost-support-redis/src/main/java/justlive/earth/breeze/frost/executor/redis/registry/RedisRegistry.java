package justlive.earth.breeze.frost.executor.redis.registry;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.core.config.ExecutorProperties;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobGroup;
import justlive.earth.breeze.frost.core.registry.AbstractRegistry;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;

@Profile("executor")
@Component
public class RedisRegistry extends AbstractRegistry {

  @Autowired
  RedissonClient redissonClient;

  @Autowired
  ExecutorProperties executorProperties;


  @Override
  public void register() {
    JobExecutor jobExecutor = this.jobExecutor();

    // 注册job监听
    for (JobGroup jobGroup : jobExecutor.getGroups()) {
      String key = String.join(SystemProperties.SEPERATOR, SystemProperties.JOB_GROUP_PREFIX,
          jobGroup.getGroupKey(), jobGroup.getJobKey());
      
      System.out.println(key);
      
      redissonClient.getExecutorService(key).registerWorkers(executorProperties.getParallel());
    }

  }

  @Override
  public void unregister() {
    throw new UnsupportedOperationException();
  }

}
