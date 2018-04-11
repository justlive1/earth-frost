package justlive.earth.breeze.frost.executor.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * redis配置
 * 
 * @author wubo
 *
 */
@Configuration
public class RedisConfig {

  /**
   * 单机模式
   * 
   * @param redssionProperties
   * @return
   */
  @Bean
  @ConditionalOnProperty(name = "redisson.mode", havingValue = "0")
  RedissonClient redissonSingle(RedissonProperties redssionProperties) {
    Config config = new Config();
    SingleServerConfig serverConfig = config.useSingleServer()
        .setAddress(redssionProperties.getAddress()).setTimeout(redssionProperties.getTimeout())
        .setConnectionPoolSize(redssionProperties.getConnectionPoolSize())
        .setConnectionMinimumIdleSize(redssionProperties.getConnectionMinimumIdleSize());

    if (StringUtils.hasText(redssionProperties.getPassword())) {
      serverConfig.setPassword(redssionProperties.getPassword());
    }

    return Redisson.create(config);
  }

  /**
   * 集群模式
   * 
   * @param redssionProperties
   * @return
   */
  @Bean
  @ConditionalOnProperty(name = "redisson.mode", havingValue = "1")
  RedissonClient redissonCluster(RedissonProperties redssionProperties) {
    Config config = new Config();
    ClusterServersConfig serverConfig =
        config.useClusterServers().addNodeAddress(redssionProperties.getSentinelAddresses())
            .setTimeout(redssionProperties.getTimeout())
            .setSlaveConnectionPoolSize(redssionProperties.getSlaveConnectionPoolSize())
            .setMasterConnectionPoolSize(redssionProperties.getMasterConnectionPoolSize());

    if (StringUtils.hasText(redssionProperties.getPassword())) {
      serverConfig.setPassword(redssionProperties.getPassword());
    }

    return Redisson.create(config);
  }

}
