package vip.justlive.frost.core.notify;

import org.redisson.Redisson;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.oxygen.core.ioc.Bean;
import vip.justlive.oxygen.core.ioc.Inject;

/**
 * redis实现的事件发布
 * 
 * @author wubo
 *
 */
@Bean
public class RedisEventPublisherImpl implements EventPublisher {

  private final RedissonClient redissonClient;

  @Inject
  public RedisEventPublisherImpl(Redisson redissonClient) {
    this.redissonClient = redissonClient;
  }

  @Override
  public void publish(Event event) {
    RScheduledExecutorService executor = redissonClient.getExecutorService(
        String.join(JobConfig.SEPERATOR, JobConfig.CENTER_PREFIX, EventPublisher.class.getName()));
    executor.execute(new EventExecuteWrapper(event));
  }

}
