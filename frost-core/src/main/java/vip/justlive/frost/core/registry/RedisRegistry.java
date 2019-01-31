package vip.justlive.frost.core.registry;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * redis注册实现类
 *
 * @author wubo
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

    List<JobGroup> jobGroups = jobExecutor.getGroups();
    if (jobGroups == null || jobGroups.isEmpty()) {
      log.info("no jobs need to register");
      return;
    }
    // 注册job执行器
    for (JobGroup jobGroup : jobGroups) {
      String key = String
          .format(JobConfig.JOB_BEAN_CHANNEL, jobGroup.getGroupKey(), jobGroup.getJobKey());
      log.info("register job [{}]", key);
      redissonClient.getExecutorService(key).registerWorkers(JobConfig.getParallel());
    }

    // script执行器
    if (JobConfig.getExecutor().getScriptJobEnabled()) {
      redissonClient.getExecutorService(String.format(JobConfig.JOB_SCRIPT_CHANNEL, ""))
          .registerWorkers(JobConfig.getParallel());
      redissonClient
          .getExecutorService(String.format(JobConfig.JOB_SCRIPT_CHANNEL, jobExecutor.getKey()))
          .registerWorkers(JobConfig.getParallel());
    }

    // 订阅worker
    redissonClient.<String>getTopic(JobConfig.WORKER_REGISTER).addListener((channel, uuid) -> {
      redissonClient.getMapCache(String.format(JobConfig.WORKER_REQ_VAL, uuid))
          .put(jobExecutor.getId(), jobExecutor, 20, TimeUnit.SECONDS);
      redissonClient.getSemaphore(String.format(JobConfig.WORKER_REQ, uuid)).release();
    });
  }

  @Override
  public void unregister() {
  }

}
