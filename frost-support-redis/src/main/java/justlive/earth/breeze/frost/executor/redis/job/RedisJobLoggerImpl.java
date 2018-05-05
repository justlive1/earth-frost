package justlive.earth.breeze.frost.executor.redis.job;

import java.util.List;
import java.util.UUID;
import org.redisson.api.RListMultimap;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.core.config.JobProperties;
import justlive.earth.breeze.frost.core.job.JobLogger;

/**
 * redis实现
 * 
 * @author wubo
 *
 */
@Component
public class RedisJobLoggerImpl implements JobLogger {

  @Autowired
  RedissonClient redissonClient;

  @Override
  public String bindLog(String jobId) {
    String id = UUID.randomUUID().toString();
    RMap<String, String> map = redissonClient.getMap(String.join(JobProperties.SEPERATOR,
        JobProperties.CENTER_PREFIX, JobLogger.class.getName()));
    RListMultimap<String, String> listmap = redissonClient
        .getListMultimap(String.join(JobProperties.SEPERATOR, JobProperties.CENTER_PREFIX,
            JobLogger.class.getName(), RListMultimap.class.getSimpleName()));
    map.put(id, jobId);
    listmap.put(jobId, id);
    return id;
  }

  @Override
  public void removeLogger(String jobId) {
    RMap<String, String> map = redissonClient.getMap(String.join(JobProperties.SEPERATOR,
        JobProperties.CENTER_PREFIX, JobLogger.class.getName()));
    RListMultimap<String, String> listmap = redissonClient
        .getListMultimap(String.join(JobProperties.SEPERATOR, JobProperties.CENTER_PREFIX,
            JobLogger.class.getName(), RListMultimap.class.getSimpleName()));
    List<String> list = listmap.removeAll(jobId);
    for (String id : list) {
      map.remove(id);
    }
  }
}
