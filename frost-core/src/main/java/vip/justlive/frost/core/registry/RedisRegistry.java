package vip.justlive.frost.core.registry;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.config.SystemProperties;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * redis注册实现类
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class RedisRegistry implements Registry {

  private final RedissonClient redissonClient;
  private final JobExecutor jobExecutor;

  @Override
  public void register() {

    List<JobGroup> jobGroups = jobExecutor.getGroups();
    if (jobGroups == null || jobGroups.isEmpty()) {
      log.info("no jobs need to register");
      return;
    }

    SystemProperties systemProperties = Container.get().getSystemProperties();

    ThreadPoolExecutor pool = ThreadUtils
        .newThreadPool(systemProperties.getCorePoolSize(), systemProperties.getMaximumPoolSize(),
            systemProperties.getKeepAliveTime(), systemProperties.getQueueCapacity(),
            "frost-executor-%d");

    // 注册job执行器
    for (JobGroup jobGroup : jobGroups) {
      String key = String
          .format(Container.JOB_BEAN_CHANNEL, jobGroup.getGroupKey(), jobGroup.getJobKey());
      log.info("register job [{}]", key);
      redissonClient.getExecutorService(key).registerWorkers(systemProperties.getParallel(), pool);
    }

    // script执行器
    if (Container.get().getJobExecutorProperties().getScriptJobEnabled()) {
      redissonClient.getExecutorService(String.format(Container.JOB_SCRIPT_CHANNEL, ""))
          .registerWorkers(systemProperties.getParallel(), pool);
      redissonClient
          .getExecutorService(String.format(Container.JOB_SCRIPT_CHANNEL, jobExecutor.getKey()))
          .registerWorkers(systemProperties.getParallel(), pool);
    }

    // 订阅worker
    redissonClient.<String>getTopic(Container.WORKER_REGISTER).addListener((channel, uuid) -> {
      redissonClient.getMapCache(String.format(Container.WORKER_REQ_VAL, uuid))
          .put(jobExecutor.getId(), jobExecutor, 20, TimeUnit.SECONDS);
      redissonClient.getSemaphore(String.format(Container.WORKER_REQ, uuid)).release();
    });
  }

  @Override
  public void unregister() {
  }

}
