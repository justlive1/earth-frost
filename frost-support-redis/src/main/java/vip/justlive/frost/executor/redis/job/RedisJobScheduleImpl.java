package vip.justlive.frost.executor.redis.job;

import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.redisson.api.CronSchedule;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RScheduledFuture;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.common.base.exception.Exceptions;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.config.JobProperties;
import vip.justlive.frost.core.job.JobDispatchWrapper;
import vip.justlive.frost.core.job.JobSchedule;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.frost.core.registry.HeartBeat;

/**
 * redis任务定时实现
 * 
 * @author wubo
 *
 */
@Slf4j
@Profile(JobProperties.PROFILE_CENTER)
@Component
public class RedisJobScheduleImpl implements JobSchedule {

  @Autowired
  RedissonClient redissonClient;

  @Autowired
  @Qualifier("redisson-executor")
  ExecutorService executorService;

  @Autowired
  JobProperties jobProps;

  @Autowired
  JobRepository jobRepository;

  @PostConstruct
  void init() {

    // 心跳
    RTopic<HeartBeat> topic = redissonClient.getTopic(String.join(JobProperties.SEPERATOR,
        JobProperties.EXECUTOR_PREFIX, HeartBeat.class.getName()));
    topic.addListener((channel, msg) -> {
      if (log.isDebugEnabled()) {
        log.debug("heartBeat: {}", msg);
      }
    });

    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);
    service.registerWorkers(jobProps.getParallel(), executorService);
  }

  @Override
  public String addJob(String jobId) {
    JobInfo jobInfo = this.getJobInfo(jobId);
    switch (JobInfo.MODE.valueOf(jobInfo.getMode())) {
      case SIMPLE:
        return this.addSimpleJob(jobId, jobInfo.getTimestamp());
      case DELAY:
        return this.addDelayJob(jobId, jobInfo.getInitDelay(), jobInfo.getDelay());
      case CRON:
        return this.addCronJob(jobId, jobInfo.getCron());
      default:
        return null;
    }
  }

  @Override
  public String addSimpleJob(String jobId, long timestamp) {

    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);
    long delay = timestamp - ZonedDateTime.now().toInstant().toEpochMilli();
    RScheduledFuture<?> future =
        service.scheduleAsync(new JobDispatchWrapper(jobId), delay, TimeUnit.MILLISECONDS);

    String taskId = future.getTaskId();
    RListMultimap<String, String> listmap = redissonClient.getListMultimap(String
        .join(JobProperties.SEPERATOR, JobProperties.EXECUTOR_PREFIX, JobSchedule.class.getName()));
    listmap.put(jobId, taskId);

    return taskId;
  }

  @Override
  public String addDelayJob(String jobId, long initDelay, long delay) {

    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);

    RScheduledFuture<?> future = service.scheduleAtFixedRateAsync(new JobDispatchWrapper(jobId),
        initDelay, delay, TimeUnit.SECONDS);

    String taskId = future.getTaskId();
    RListMultimap<String, String> listmap = redissonClient.getListMultimap(String
        .join(JobProperties.SEPERATOR, JobProperties.EXECUTOR_PREFIX, JobSchedule.class.getName()));
    listmap.put(jobId, taskId);

    return taskId;
  }

  @Override
  public String addCronJob(String jobId, String cron) {

    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);
    RScheduledFuture<?> future =
        service.scheduleAsync(new JobDispatchWrapper(jobId), CronSchedule.of(cron));

    String taskId = future.getTaskId();
    RListMultimap<String, String> listmap = redissonClient.getListMultimap(String
        .join(JobProperties.SEPERATOR, JobProperties.EXECUTOR_PREFIX, JobSchedule.class.getName()));
    listmap.put(jobId, taskId);

    return taskId;
  }

  @Override
  public String refreshJob(String jobId, String cron) {

    this.pauseJob(jobId);
    return this.addCronJob(jobId, cron);
  }

  @Override
  public String refreshJob(String jobId, Long timestamp) {

    this.pauseJob(jobId);
    return this.addSimpleJob(jobId, timestamp);
  }

  @Override
  public String refreshJob(String jobId, Long initDelay, Long delay) {
   
    this.pauseJob(jobId);
    return this.addDelayJob(jobId, initDelay, delay);
  }

  @Override
  public void pauseJob(String jobId) {

    RListMultimap<String, String> listmap = redissonClient.getListMultimap(String
        .join(JobProperties.SEPERATOR, JobProperties.EXECUTOR_PREFIX, JobSchedule.class.getName()));
    RList<String> list = listmap.get(jobId);
    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);
    for (String id : list) {
      service.cancelTask(id);
    }
    listmap.removeAll(jobId);
  }

  @Override
  public String resumeJob(String jobId) {
    pauseJob(jobId);
    return addJob(jobId);
  }

  @Override
  public void removeJob(String jobId) {

    this.pauseJob(jobId);
  }

  @Override
  public void triggerJob(String jobId) {

    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);
    service.submit(new JobDispatchWrapper(jobId));
  }

  @Override
  public void retryJob(String jobId, String loggerId, String parentLoggerId) {

    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);
    JobDispatchWrapper wrapper = new JobDispatchWrapper(jobId, loggerId);
    wrapper.setParentLoggerId(parentLoggerId);
    service.submit(wrapper);
  }

  @Override
  public void triggerChildJob(String jobId, String loggerId) {
    RScheduledExecutorService service =
        redissonClient.getExecutorService(JobProperties.CENTER_PREFIX);
    JobDispatchWrapper wrapper = new JobDispatchWrapper(jobId);
    wrapper.setParentLoggerId(loggerId);
    service.submit(wrapper);
  }

  private JobInfo getJobInfo(String jobId) {
    JobInfo jobInfo = jobRepository.findJobInfoById(jobId);
    if (jobInfo == null) {
      throw Exceptions.fail("300002", "未查询到Job记录");
    }
    return jobInfo;
  }
}
