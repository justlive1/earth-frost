package justlive.earth.breeze.frost.executor.redis.job;

import java.util.UUID;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.core.job.JobLogger;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;

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
    RMap<String, String> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.CENTER_PREFIX, JobLogger.class.getName()));
    map.put(jobId, id);
    return id;
  }

  @Override
  public String findLoggerId(String jobId) {
    RMap<String, String> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.CENTER_PREFIX, JobLogger.class.getName()));
    return map.get(jobId);
  }

  @Override
  public void removeLogger(String jobId) {
    RMap<String, String> map = redissonClient.getMap(String.join(SystemProperties.SEPERATOR,
        SystemProperties.CENTER_PREFIX, JobLogger.class.getName()));
    map.remove(jobId);
  }
}
