package justlive.earth.breeze.frost.executor.redis.service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.redisson.api.RMapCache;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.registry.HeartBeat;
import justlive.earth.breeze.frost.core.service.CenterService;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * redis调度服务实现类
 * 
 * @author wubo
 *
 */
@Profile("center")
@Slf4j
@Service
public class RedisCenterServiceImpl implements CenterService {

  @Autowired
  RedissonClient redissonClient;

  @PostConstruct
  void init() {
    RTopic<HeartBeat> topic = redissonClient.getTopic(String.join(SystemProperties.SEPERATOR,
        SystemProperties.EXECUTOR_PREFIX, SystemProperties.TOPIC));
    topic.addListener((channel, msg) -> {
      if (log.isDebugEnabled())
        log.debug("heartBeat: {}", msg);
    });
  }

  @Override
  public List<JobExecutor> queryActiveExecutors() {
    RMapCache<String, JobExecutor> cache =
        redissonClient.getMapCache(String.join(SystemProperties.SEPERATOR,
            SystemProperties.EXECUTOR_PREFIX, SystemProperties.CACHE));
    return new ArrayList<>(cache.values());
  }

}
