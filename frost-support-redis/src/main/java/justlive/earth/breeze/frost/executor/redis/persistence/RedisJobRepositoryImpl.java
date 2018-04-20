package justlive.earth.breeze.frost.executor.redis.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.google.common.collect.Lists;
import justlive.earth.breeze.frost.core.model.HashRef;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.core.persistence.JobRepository;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;
import justlive.earth.breeze.snow.common.base.util.Checks;

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
    listMultimap.put(record.getJobId(), record);
    HashRef ref =
        new HashRef(record.getJobId(), listMultimap.get(record.getJobId()).indexOf(record));
    RMap<String, HashRef> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, HashRef.class.getName()));
    map.put(record.getId(), ref);
    return record.getId();
  }

  @Override
  public List<JobExecuteRecord> queryJobRecords(String jobId, int from, int to) {
    RListMultimap<String, JobExecuteRecord> listMultimap =
        redissonClient.getListMultimap(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, JobExecuteRecord.class.getName()));
    if (StringUtils.isNoneBlank(jobId)) {
      List<JobExecuteRecord> list = listMultimap.get(jobId);
      if (list.size() <= from) {
        return Collections.emptyList();
      }
      return Lists.newArrayList(listMultimap.get(jobId).subList(from, Math.min(to, list.size())));
    }
    // TODO 查询优化
    return listMultimap.values().stream().skip(from).limit(Math.min(to, listMultimap.size()))
        .collect(Collectors.toList());
  }

  @Override
  public JobExecuteRecord findJobExecuteRecordById(String id) {
    RMap<String, HashRef> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, HashRef.class.getName()));
    HashRef ref = map.get(id);
    Checks.notNull(ref);
    RListMultimap<String, JobExecuteRecord> listMultimap =
        redissonClient.getListMultimap(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, JobExecuteRecord.class.getName()));
    return listMultimap.get(ref.getKey()).get(ref.getIndex());
  }

  @Override
  public void updateJobRecord(JobExecuteRecord record) {
    JobExecuteRecord localRecord = findJobExecuteRecordById(record.getId());
    localRecord.setDispachMsg(record.getDispachMsg());
    localRecord.setDispachStatus(record.getDispachStatus());
    localRecord.setDispachTime(record.getDispachTime());
    localRecord.setExecuteMsg(record.getExecuteMsg());
    localRecord.setExecuteStatus(record.getExecuteStatus());
    localRecord.setExecuteTime(record.getExecuteTime());

    RMap<String, HashRef> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, HashRef.class.getName()));
    HashRef ref = map.get(record.getId());

    RListMultimap<String, JobExecuteRecord> listMultimap =
        redissonClient.getListMultimap(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, JobExecuteRecord.class.getName()));
    listMultimap.get(localRecord.getJobId()).set(ref.getIndex(), localRecord);
  }
}
