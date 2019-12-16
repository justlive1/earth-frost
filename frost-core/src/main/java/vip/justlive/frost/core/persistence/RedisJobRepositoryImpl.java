package vip.justlive.frost.core.persistence;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RList;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RSemaphore;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobRecordStatus;
import vip.justlive.frost.api.model.JobScript;
import vip.justlive.frost.core.config.Container;
import vip.justlive.oxygen.core.util.Strings;

/**
 * redis持久化实现
 *
 * @author wubo
 */
public class RedisJobRepositoryImpl implements JobRepository {

  private final RedissonClient redissonClient;
  private final RTopic<String> workerTopic;

  public RedisJobRepositoryImpl(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
    this.workerTopic = redissonClient.getTopic(Container.WORKER_REGISTER);
  }

  private void waitFor(String uuid, int subscribers) {
    RSemaphore semaphore = redissonClient.getSemaphore(String.format(Container.WORKER_REQ, uuid));
    try {
      semaphore.tryAcquire(subscribers, 10L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    redissonClient.getKeys().delete(semaphore);
  }

  @Override
  public int countExecutors() {
    String uuid = UUID.randomUUID().toString();
    int subscribers = (int) workerTopic.publish(uuid);
    if (subscribers == 0) {
      return 0;
    }
    RMapCache<String, JobExecutor> cache = redissonClient
        .getMapCache(String.format(Container.WORKER_REQ_VAL, uuid));
    waitFor(uuid, subscribers);
    return cache.size();
  }

  @Override
  public List<JobExecutor> queryJobExecutors() {
    String uuid = UUID.randomUUID().toString();
    int subscribers = (int) workerTopic.publish(uuid);
    if (subscribers == 0) {
      return Collections.emptyList();
    }
    RMapCache<String, JobExecutor> cache = redissonClient
        .getMapCache(String.format(Container.WORKER_REQ_VAL, uuid));
    waitFor(uuid, subscribers);
    return new ArrayList<>(cache.readAllValues());
  }

  @Override
  public void addJob(JobInfo jobInfo) {
    RMap<String, JobInfo> map = redissonClient.getMap(Container.JOB_INFO);
    jobInfo.setId(UUID.randomUUID().toString());
    RList<String> sortList = redissonClient.getList(Container.JOB_INFO_SORT);
    sortList.add(jobInfo.getId());
    // script
    if (JobInfo.TYPE.SCRIPT.name().equals(jobInfo.getType())) {
      JobScript script = build(jobInfo);
      redissonClient.<String, JobScript>getListMultimap(Container.JOB_INFO_SCRIPT)
          .put(jobInfo.getId(), script);
    }
    jobInfo.setScript(null);
    map.put(jobInfo.getId(), jobInfo);
  }

  @Override
  public void updateJob(JobInfo jobInfo) {
    RMap<String, JobInfo> map = redissonClient.getMap(Container.JOB_INFO);
    // script
    RListMultimap<String, JobScript> scriptList = redissonClient
        .getListMultimap(Container.JOB_INFO_SCRIPT);
    if (jobInfo.getScript() != null && JobInfo.TYPE.SCRIPT.name().equals(jobInfo.getType())) {
      JobInfo local = map.get(jobInfo.getId());
      if (!Objects.equals(jobInfo.getType(), local.getType())) {
        JobScript script = build(jobInfo);
        scriptList.removeAll(jobInfo.getId());
        scriptList.put(jobInfo.getId(), script);
      }
    } else if (JobInfo.TYPE.BEAN.name().equals(jobInfo.getType())) {
      scriptList.removeAll(jobInfo.getId());
    }
    jobInfo.setScript(null);
    map.put(jobInfo.getId(), jobInfo);
  }


  @Override
  public void removeJob(String jobId) {
    redissonClient.<String, JobInfo>getMap(Container.JOB_INFO).remove(jobId);
    redissonClient.<String>getList(Container.JOB_INFO_SORT).remove(jobId);
  }

  @Override
  public int countJobInfos() {
    return redissonClient.<String, JobInfo>getMap(Container.JOB_INFO).size();
  }

  @Override
  public List<JobInfo> queryJobInfos(int from, int to) {
    RMap<String, JobInfo> map = redissonClient.getMap(Container.JOB_INFO);
    RList<String> list = redissonClient.getList(Container.JOB_INFO_SORT);
    List<JobInfo> result = new ArrayList<>();
    for (String id : list.subList(from, Math.min(to, list.size()))) {
      result.add(map.get(id));
    }
    return result;
  }

  @Override
  public List<JobInfo> queryAllJobs() {
    return new ArrayList<>(
        redissonClient.<String, JobInfo>getMap(Container.JOB_INFO).readAllValues());
  }

  @Override
  public JobInfo findJobInfoById(String id) {
    RMap<String, JobInfo> map = redissonClient.getMap(Container.JOB_INFO);
    JobInfo jobInfo = map.get(id);
    if (jobInfo == null) {
      return null;
    }
    RListMultimap<String, JobScript> scriptList = redissonClient
        .getListMultimap(Container.JOB_INFO_SCRIPT);
    RList<JobScript> list = scriptList.get(id);
    int size = list.size();
    if (size > 0) {
      JobScript script = list.get(size - 1);
      jobInfo.setScript(script.getScript());
    }
    return jobInfo;
  }

  @Override
  public String addJobRecord(JobExecuteRecord record) {
    RMap<String, JobExecuteRecord> map = redissonClient.getMap(Container.RECORD);
    map.put(record.getId(), record);

    // 全部
    RListMultimap<String, String> sortmap = redissonClient.getListMultimap(Container.RECORD_SORT);
    sortmap.put(Strings.EMPTY, record.getId());
    // jobId
    sortmap.put(record.getJobId(), record.getId());

    JobInfo info = findJobInfoById(record.getJobId());
    JobGroup group = info.getGroup();
    if (Objects.equals(JobInfo.TYPE.BEAN.name(), info.getType())) {
      // groupKey
      sortmap.put(group.getGroupKey(), record.getId());
      // jobKey
      sortmap
          .put(String.join(Strings.COLON, group.getGroupKey(), group.getJobKey()), record.getId());
    } else if (group != null && group.getGroupKey() != null) {
      // groupKey
      sortmap.put(group.getGroupKey(), record.getId());
    }
    return record.getId();
  }

  @Override
  public int countJobRecords(String groupKey, String jobKey, String jobId) {
    RListMultimap<String, String> sortmap = redissonClient.getListMultimap(Container.RECORD_SORT);
    if (jobId != null && jobId.length() > 0) {
      return sortmap.get(jobId).size();
    }
    String key = key(groupKey, jobKey);
    return sortmap.get(key).size();
  }

  @Override
  public List<JobExecuteRecord> queryJobRecords(String groupKey, String jobKey, String jobId,
      int from, int to) {
    RListMultimap<String, String> sortmap = redissonClient.getListMultimap(Container.RECORD_SORT);
    List<JobExecuteRecord> records = new ArrayList<>();
    if (jobId != null && jobId.length() > 0) {
      RList<String> list = sortmap.get(jobId);
      fill(list, records, from, to);
      return records;
    }
    String key = key(groupKey, jobKey);
    RList<String> list = sortmap.get(key);
    fill(list, records, from, to);
    return records;
  }

  @Override
  public JobExecuteRecord findJobExecuteRecordById(String id) {
    RMap<String, JobExecuteRecord> map = redissonClient.getMap(Container.RECORD);
    JobExecuteRecord record = map.get(id);
    RListMultimap<String, JobRecordStatus> recordStatus = redissonClient
        .getListMultimap(Container.RECORD_STATUS);
    List<JobRecordStatus> statuses = recordStatus.getAll(id);
    statuses.forEach(r -> r.fill(record));
    record.setRecordStatuses(statuses);
    return record;
  }

  @Override
  public void addJobRecordStatus(JobRecordStatus recordStatus) {
    RListMultimap<String, JobRecordStatus> listMultimap = redissonClient
        .getListMultimap(Container.RECORD_STATUS);
    listMultimap.put(recordStatus.getLoggerId(), recordStatus);
  }

  @Override
  public void removeJobRecords(String jobId) {
    RMap<String, JobExecuteRecord> map = redissonClient.getMap(Container.RECORD);
    RListMultimap<String, String> sortmap = redissonClient.getListMultimap(Container.RECORD_SORT);
    List<String> list = sortmap.removeAll(jobId);
    RListMultimap<String, JobRecordStatus> statusMultimap = redissonClient
        .getListMultimap(Container.RECORD_STATUS);
    JobGroup group = findJobInfoById(jobId).getGroup();
    for (String key : list) {
      sortmap.get(Strings.EMPTY).remove(key);
      if (group != null) {
        sortmap.get(group.getGroupKey()).remove(key);
        sortmap.get(String.join(Strings.COLON, group.getGroupKey(), group.getJobKey())).remove(key);
      }
      statusMultimap.removeAll(key);
      map.remove(key);
      redissonClient.getKeys().delete(String.format(Container.EVENT_SHARDING, jobId, key));
    }
  }

  @Override
  public void addJobScript(JobScript script) {
    RListMultimap<String, JobScript> scriptList = redissonClient
        .getListMultimap(Container.JOB_INFO_SCRIPT);
    script.setId(UUID.randomUUID().toString());
    script.setTime(Date.from(ZonedDateTime.now().toInstant()));
    scriptList.put(script.getJobId(), script);
    if (scriptList.get(script.getJobId()).size() > 20) {
      scriptList.get(script.getJobId()).remove(0);
    }
  }

  @Override
  public List<JobScript> queryJobScripts(String jobId) {
    return redissonClient.<String, JobScript>getListMultimap(Container.JOB_INFO_SCRIPT)
        .getAll(jobId);
  }

  @Override
  public void removeJobScripts(String jobId) {
    redissonClient.<String, JobScript>getListMultimap(Container.JOB_INFO_SCRIPT).removeAll(jobId);
  }

  @Override
  public long currentTime() {
    return redissonClient.getScript()
        .eval(Mode.READ_ONLY, "return redis.call('TIME')[1]*1000", ReturnType.INTEGER);
  }

  @Override
  public long countByType(String type) {
    return redissonClient.getAtomicLong(type).get();
  }

  private JobScript build(JobInfo jobInfo) {
    JobScript script = new JobScript();
    script.setId(UUID.randomUUID().toString());
    script.setJobId(jobInfo.getId());
    script.setScript(jobInfo.getScript());
    script.setTime(Date.from(ZonedDateTime.now().toInstant()));
    script.setVersion("default");
    return script;
  }

  private String key(String groupKey, String jobKey) {
    String key = Strings.EMPTY;
    if (groupKey != null && groupKey.length() > 0) {
      key = groupKey;
    }
    if (jobKey != null && jobKey.length() > 0) {
      key = String.join(Strings.COLON, key, jobKey);
    }
    return key;
  }

  private void fill(RList<String> list, List<JobExecuteRecord> records, int from, int to) {
    if (list.size() <= from) {
      return;
    }
    for (String id : list.subList(from, Math.min(to, list.size()))) {
      records.add(findJobExecuteRecordById(id));
    }
  }
}
