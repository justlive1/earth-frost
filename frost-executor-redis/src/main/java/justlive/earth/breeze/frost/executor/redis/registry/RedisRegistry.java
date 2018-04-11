package justlive.earth.breeze.frost.executor.redis.registry;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.core.registry.Registry;
import justlive.earth.breeze.frost.executor.config.ExecutorProperties;

@Component
public class RedisRegistry implements Registry {

  @Autowired
  RedissonClient redissonClient;

  @Autowired
  ExecutorProperties executorProps;

  @Override
  public void register() {

  }

  @Override
  public void unregister() {
    // TODO Auto-generated method stub

  }

}
