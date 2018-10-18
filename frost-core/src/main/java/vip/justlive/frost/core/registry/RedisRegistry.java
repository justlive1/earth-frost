package vip.justlive.frost.core.registry;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RMapCache;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.util.ThreadUtils;

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
  private final ScheduledExecutorService scheduledExecutor;

  private RTopic<HeartBeat> topic;
  private ScheduledFuture<?> beatFuture;

  public RedisRegistry() {
    this.redissonClient = BeanStore.getBean(RedissonClient.class);
    this.scheduledExecutor = ThreadUtils.newScheduledExecutor(1, "heartbeat-schedule-pool-%d");
    this.jobExecutor = JobConfig.getJobExecutor();
  }

  @Override
  public void register() {

    // 订阅心跳检测
    topic = redissonClient.getTopic(
        String.join(JobConfig.SEPERATOR, JobConfig.EXECUTOR_PREFIX, HeartBeat.class.getName()));

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

    // 心跳任务
    beatFuture = scheduledExecutor.scheduleWithFixedDelay(() -> {
      try {
        long subscribes = topic.publish(new HeartBeat(jobExecutor.getAddress(),
            HeartBeat.TYPE.PING.name(), jobExecutor.getName()));
        if (subscribes == 0) {
          log.warn("未发现调度中心服务");
        }
        RMapCache<String, JobExecutor> cache = redissonClient.getMapCache(String
            .join(JobConfig.SEPERATOR, JobConfig.EXECUTOR_PREFIX, JobExecutor.class.getName()));
        cache.put(jobExecutor.getId(), jobExecutor, JobConfig.HEARTBEAT, TimeUnit.SECONDS);
      } catch (Exception e) {
        log.error("心跳任务出错 ", e);
      }
    }, JobConfig.HEARTBEAT, JobConfig.HEARTBEAT, TimeUnit.SECONDS);

    // 注册事件
    topic.publish(new HeartBeat(jobExecutor.getAddress(), HeartBeat.TYPE.REGISTER.name(),
        jobExecutor.getName()));

  }

  @Override
  public void unregister() {
    beatFuture.cancel(true);
    topic.publish(new HeartBeat(jobExecutor.getAddress(), HeartBeat.TYPE.UNREGISTER.name(),
        jobExecutor.getName()));
  }

}
