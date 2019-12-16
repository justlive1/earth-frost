package vip.justlive.frost.core.job;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobRecordStatus;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.util.Strings;

/**
 * redis实现
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class RedisJobLoggerImpl implements JobLogger {

  private final RedissonClient redissonClient;
  private final JobRepository jobRepository;

  @Override
  public String bindLog(String jobId) {
    String id = UUID.randomUUID().toString();
    redissonClient.<String, String>getMap(Container.LOG_BIND).put(id, jobId);
    redissonClient.<String, String>getListMultimap(Container.LOG_REL).put(jobId, id);
    return id;
  }

  @Override
  public void removeLogger(String jobId) {
    RMap<String, String> map = redissonClient.getMap(Container.LOG_BIND);
    for (String id : redissonClient.<String, String>getListMultimap(Container.LOG_REL)
        .removeAll(jobId)) {
      map.remove(id);
    }
  }

  @Override
  public void enter(String loggerId, String type) {
    // 运行中 ++
    redissonClient.getAtomicLong(Container.STAT_TOTAL_RUNNING).incrementAndGet();
    redissonClient.getAtomicLong(String.format(Container.STAT_TOTAL_TYPE, type)).incrementAndGet();
  }

  @Override
  public void leave(String loggerId, String type, boolean success) {
    // 运行中 --
    redissonClient.getAtomicLong(Container.STAT_TOTAL_RUNNING).decrementAndGet();
    // 每日统计
    String date = DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now());
    String key;
    if (success) {
      key = String.format(Container.STAT_DATE_TYPE_SUCCESS, date, type);
    } else {
      key = String.format(Container.STAT_DATE_TYPE_FAIL, date, type);
    }
    redissonClient.getAtomicLong(key).incrementAndGet();
    if (Container.STAT_TYPE_EXECUTE.equals(type)) {
      removeOldestLogger(loggerId);
    }
  }

  private void removeOldestLogger(String loggerId) {
    long maxLogSize = Container.get().getJobExecutorProperties().getMaxLogSize();
    if (maxLogSize < 0) {
      return;
    }

    RMap<String, String> logMap = redissonClient.getMap(Container.LOG_BIND);
    String jobId = logMap.get(loggerId);
    if (jobId == null) {
      return;
    }

    RListMultimap<String, String> sortmap = redissonClient.getListMultimap(Container.RECORD_SORT);
    RList<String> list = sortmap.get(jobId);
    long size = list.size() - maxLogSize;
    if (size <= 0) {
      return;
    }

    JobInfo job = jobRepository.findJobInfoById(jobId);
    if (job == null) {
      return;
    }

    RListMultimap<String, JobRecordStatus> statusMultimap = redissonClient
        .getListMultimap(Container.RECORD_STATUS);
    RMap<String, JobExecuteRecord> map = redissonClient.getMap(Container.RECORD);
    RList<String> logIds = redissonClient.<String, String>getListMultimap(Container.LOG_REL)
        .get(jobId);

    JobGroup group = job.getGroup();
    for (String key : list.subList(0, (int) size).readAll()) {
      sortmap.get(Strings.EMPTY).remove(key);
      if (group != null) {
        sortmap.get(String.join(Strings.COLON, group.getGroupKey(), group.getJobKey())).remove(key);
        sortmap.get(group.getGroupKey()).remove(key);
      }
      statusMultimap.removeAll(key);
      list.remove(key);
      map.remove(key);
      logMap.remove(key);
      logIds.remove(key);
      redissonClient.getKeys().delete(String.format(Container.EVENT_SHARDING, jobId, loggerId));
    }

  }
}
