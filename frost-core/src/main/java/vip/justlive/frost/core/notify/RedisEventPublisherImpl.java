package vip.justlive.frost.core.notify;

import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.core.config.Container;

/**
 * redis实现的事件发布
 *
 * @author wubo
 */
public class RedisEventPublisherImpl implements EventPublisher {

  private final RScheduledExecutorService executorService;

  public RedisEventPublisherImpl(RedissonClient redissonClient) {
    this.executorService = redissonClient.getExecutorService(Container.EVENT);
  }

  @Override
  public void publish(Event event) {
    executorService.execute(new EventExecuteWrapper(event));
  }

}
