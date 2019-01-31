package vip.justlive.frost.core.job;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.oxygen.core.ioc.Bean;
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
  }
}
