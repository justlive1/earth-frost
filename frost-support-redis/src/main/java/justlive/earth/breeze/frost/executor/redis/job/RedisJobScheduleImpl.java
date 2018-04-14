package justlive.earth.breeze.frost.executor.redis.job;

import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import org.redisson.api.CronSchedule;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RScheduledFuture;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.core.config.JobProperties;
import justlive.earth.breeze.frost.core.job.JobSchedule;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.registry.HeartBeat;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * redis任务定时实现
 * 
 * @author wubo
 *
 */
@Slf4j
@Profile(SystemProperties.PROFILE_CENTER)
@Component
public class RedisJobScheduleImpl implements JobSchedule {

  @Autowired
  RedissonClient redissonClient;

  @Autowired
  @Qualifier("redisson-executor")
  ExecutorService executorService;

  @Autowired
  JobProperties jobProps;

  @PostConstruct
  void init() {

    // 心跳
    RTopic<HeartBeat> topic = redissonClient.getTopic(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, HeartBeat.class.getName()));
    topic.addListener((channel, msg) -> {
      if (log.isDebugEnabled())
        log.debug("heartBeat: {}", msg);
    });

    RScheduledExecutorService service =
        redissonClient.getExecutorService(SystemProperties.CENTER_PREFIX);
    service.registerWorkers(jobProps.getParallel(), executorService);
  }

  @Override
  public String addJob(JobInfo jobInfo) {

    RScheduledExecutorService service =
        redissonClient.getExecutorService(SystemProperties.CENTER_PREFIX);
    RScheduledFuture<?> future = service.scheduleAsync(new JobDispatchWrapper(jobInfo.getId()),
        CronSchedule.of(jobInfo.getTriggle()));

    return future.getTaskId();
  }

}
