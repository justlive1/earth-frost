package justlive.earth.breeze.frost.executor.redis.registry;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.executor.registry.AbstractRegistry;

@Component
public class RedisRegistry extends AbstractRegistry {

  @Autowired
  RedissonClient redissonClient;

  @Override
  public void register() {

  }

  @Override
  public void unregister() {
    // TODO Auto-generated method stub

  }

}
