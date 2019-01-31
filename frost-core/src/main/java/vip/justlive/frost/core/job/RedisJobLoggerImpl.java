package vip.justlive.frost.core.job;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobRecordStatus;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.ioc.Bean;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.ioc.Inject;

/**
 * redis实现
 *
 * @author wubo
 */
@Bean
public class RedisJobLoggerImpl implements JobLogger {

  private final RedissonClient redissonClient;

  @Inject
  public RedisJobLoggerImpl(Redisson redissonClient) {
    this.redissonClient = redissonClient;
  }

  @Override
  public String bindLog(String jobId) {
    String id = UUID.randomUUID().toString();
    redissonClient.<String, String>getMap(JobConfig.LOG_BIND).put(id, jobId);
    redissonClient.<String, String>getListMultimap(JobConfig.LOG_REL).put(jobId, id);
    return id;
  }

  @Override
  public void removeLogger(String jobId) {
    RMap<String, String> map = redissonClient.getMap(JobConfig.LOG_BIND);
    for (String id : redissonClient.<String, String>getListMultimap(JobConfig.LOG_REL)
        .removeAll(jobId)) {
      map.remove(id);
    }
  }

  private void removeOldestLogger(String loggerId) {
    long maxLogSize = JobConfig.getExecutor().getMaxLogSize();
    if (maxLogSize < 0) {
      return;
    }

    RMap<String, String> logMap = redissonClient.getMap(JobConfig.LOG_BIND);
    String jobId = logMap.get(loggerId);
    if (jobId == null) {
      return;
    }

    RListMultimap<String, String> sortmap = redissonClient.getListMultimap(JobConfig.RECORD_SORT);
    RList<String> list = sortmap.get(jobId);
    long size = list.size() - maxLogSize;
    if (size <= 0) {
      return;
    }

    JobInfo job = BeanStore.getBean(JobRepository.class).findJobInfoById(jobId);
    if (job == null) {
      return;
    }

    RListMultimap<String, JobRecordStatus> statusMultimap = redissonClient
        .getListMultimap(JobConfig.RECORD_STATUS);
    RMap<String, JobExecuteRecord> map = redissonClient.getMap(JobConfig.RECORD);
    RList<String> logIds = redissonClient.<String, String>getListMultimap(JobConfig.LOG_REL)
        .get(jobId);

    JobGroup group = job.getGroup();
    for (String key : list.subList(0, (int) size).readAll()) {
      sortmap.get(Constants.EMPTY).remove(key);
      if (group != null) {
        sortmap.get(group.getGroupKey()).remove(key);
        sortmap.get(String.join(Constants.COLON, group.getGroupKey(), group.getJobKey()))
            .remove(key);
      }
      statusMultimap.removeAll(key);
      list.remove(key);
      map.remove(key);
      logMap.remove(key);
      logIds.remove(key);
      redissonClient.getKeys().delete(String.format(JobConfig.EVENT_SHARDING, jobId, loggerId));
    }

  }

  @Override
  public void enter(String loggerId, String type) {
    // 运行中 ++
    redissonClient.getAtomicLong(JobConfig.STAT_TOTAL_RUNNING).incrementAndGet();
    redissonClient.getAtomicLong(String.format(JobConfig.STAT_TOTAL_TYPE, type)).incrementAndGet();
  }

  @Override
  public void leave(String loggerId, String type, boolean success) {
    // 运行中 --
    redissonClient.getAtomicLong(JobConfig.STAT_TOTAL_RUNNING).decrementAndGet();
    // 每日统计
    String date = DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now());
    String key;
    if (success) {
      key = String.format(JobConfig.STAT_DATE_TYPE_SUCCESS, date, type);
    } else {
      key = String.format(JobConfig.STAT_DATE_TYPE_FAIL, date, type);
    }
    redissonClient.getAtomicLong(key).incrementAndGet();

    removeOldestLogger(loggerId);
  }
}
