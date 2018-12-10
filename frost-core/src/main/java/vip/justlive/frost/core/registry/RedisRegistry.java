package vip.justlive.frost.core.registry;

import java.util.concurrent.TimeUnit;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * redis注册实现类
 * 
 * @author wubo
 *
 */
@Slf4j
public class RedisRegistry implements Registry {

  private final RedissonClient redissonClient;
  private final JobExecutor jobExecutor;

  public RedisRegistry() {
    this.redissonClient = BeanStore.getBean(RedissonClient.class);
    this.jobExecutor = JobConfig.getJobExecutor();
  }

  @Override
  public void register() {

    // 注册job执行器
    for (JobGroup jobGroup : jobExecutor.getGroups()) {
      String key = String.join(JobConfig.SEPERATOR, JobConfig.JOB_GROUP_PREFIX,
          jobGroup.getGroupKey(), jobGroup.getJobKey());
      log.info("register job [{}]", key);
      redissonClient.getExecutorService(key).registerWorkers(JobConfig.getParallel());
    }

    // script执行器
    if (JobConfig.getExecutor().getScriptJobEnabled()) {
      redissonClient.getExecutorService(JobConfig.JOB_SCRIPT_PREFIX)
          .registerWorkers(JobConfig.getParallel());
      redissonClient
          .getExecutorService(
              String.join(JobConfig.SEPERATOR, JobConfig.JOB_SCRIPT_PREFIX, jobExecutor.getKey()))
          .registerWorkers(JobConfig.getParallel());
    }

    // 订阅worker
    redissonClient.<String>getTopic(
        String.join(JobConfig.SEPERATOR, JobConfig.EXECUTOR_PREFIX, JobConfig.JOB_REGIST_PREFIX))
        .addListener((channel, uuid) -> {
          redissonClient
              .getMapCache(String.join(JobConfig.SEPERATOR, JobConfig.EXECUTOR_PREFIX,
                  JobExecutor.class.getName(), uuid))
              .put(jobExecutor.getId(), jobExecutor, 20, TimeUnit.SECONDS);

          redissonClient.getSemaphore(String.join(JobConfig.SEPERATOR, JobConfig.EXECUTOR_PREFIX,
              JobExecutor.class.getName(), RSemaphore.class.getSimpleName(), uuid)).release();
        });
  }

  @Override
  public void unregister() {}

}
