package justlive.earth.breeze.frost.executor.redis.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.google.common.collect.Lists;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.persistence.JobRepository;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;

/**
 * redis持久化实现
 * 
 * @author wubo
 *
 */
@Repository
public class RedisJobRepositoryImpl implements JobRepository {

  @Autowired
  RedissonClient redissonClient;

  @Override
  public int countExecutors() {
    RMapCache<String, JobExecutor> cache =
        redissonClient.getMapCache(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, JobExecutor.class.getName()));
    return cache.size();
  }

  @Override
  public List<JobExecutor> queryJobExecutors() {
    RMapCache<String, JobExecutor> cache =
        redissonClient.getMapCache(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, JobExecutor.class.getName()));
    return new ArrayList<>(cache.values());
  }

  @Override
  public void addJob(JobInfo jobInfo) {
    RMap<String, JobInfo> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, JobInfo.class.getName()));
    jobInfo.setId(UUID.randomUUID().toString());
    map.put(jobInfo.getId(), jobInfo);
  }

  @Override
  public void updateJob(JobInfo jobInfo) {
    RMap<String, JobInfo> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, JobInfo.class.getName()));
    map.put(jobInfo.getId(), jobInfo);
  }

  @Override
  public void removeJob(String jobId) {
    RMap<String, JobInfo> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, JobInfo.class.getName()));
    map.remove(jobId);
  }

  @Override
  public int countJobInfos() {
    RMap<String, JobInfo> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, JobInfo.class.getName()));
    return map.size();
  }

  @Override
  public List<JobInfo> queryJobInfos() {
    RMap<String, JobInfo> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, JobInfo.class.getName()));
    return new ArrayList<>(map.values());
  }

  @Override
  public JobInfo findJobInfoById(String id) {
    RMap<String, JobInfo> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, JobInfo.class.getName()));
    return map.get(id);
  }

  @Override
  public String addJobRecord(JobExecuteRecord record) {
    RListMultimap<String, JobExecuteRecord> listMultimap =
        redissonClient.getListMultimap(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, JobExecuteRecord.class.getName()));
    record.setId(UUID.randomUUID().toString());
    listMultimap.put(record.getJobId(), record);
    return record.getId();
  }

  @Override
  public List<JobExecuteRecord> queryJobRecords(String jobId, int from, int to) {
    RListMultimap<String, JobExecuteRecord> listMultimap =
        redissonClient.getListMultimap(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, JobExecuteRecord.class.getName()));
    List<JobExecuteRecord> list = listMultimap.get(jobId);
    if (list.size() <= from) {
      return Collections.emptyList();
    }
    return Lists.newArrayList(listMultimap.get(jobId).subList(from, Math.min(to, list.size())));
  }
}
