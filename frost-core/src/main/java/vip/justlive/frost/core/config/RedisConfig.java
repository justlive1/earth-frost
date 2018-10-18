package vip.justlive.frost.core.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.ioc.Bean;
import vip.justlive.oxygen.core.ioc.Configuration;

/**
 * redis配置
 * 
 * @author wubo
 *
 */
@Configuration
public class RedisConfig {

  /**
   * redisson client
   * 
   * @return client
   */
  @Bean
  public RedissonClient redissonClient() {
    RedissonProperties redissonProperties = ConfigFactory.load(RedissonProperties.class);
    switch (redissonProperties.getMode()) {
      case 1:
        return redissonCluster(redissonProperties);
      case 2:
        return redissonReplicatedServers(redissonProperties);
      case 3:
        return redissonuseSentinelServers(redissonProperties);
      case 4:
        return redissonuseMasterSlaveServers(redissonProperties);
      default:
        return redissonSingle(redissonProperties);
    }
  }

  /**
   * 单机模式
   * 
   * @param redissonProperties
   * @return client
   */
  private RedissonClient redissonSingle(RedissonProperties redissonProperties) {
    Config config = new Config();
    SingleServerConfig serverConfig = config.useSingleServer()
        .setAddress(redissonProperties.getAddress()).setTimeout(redissonProperties.getTimeout())
        .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
        .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize());

    if (redissonProperties.getPassword() != null && redissonProperties.getPassword().length() > 0) {
      serverConfig.setPassword(redissonProperties.getPassword());
    }

    return Redisson.create(config);
  }

  /**
   * 集群模式
   * 
   * @param redissonProperties
   * @return client
   */
  private RedissonClient redissonCluster(RedissonProperties redissonProperties) {
    Config config = new Config();
    ClusterServersConfig serverConfig =
        config.useClusterServers().addNodeAddress(redissonProperties.getNodeAddresses())
            .setTimeout(redissonProperties.getTimeout())
            .setScanInterval(redissonProperties.getScanInterval())
            .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize())
            .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize());

    if (redissonProperties.getPassword() != null && redissonProperties.getPassword().length() > 0) {
      serverConfig.setPassword(redissonProperties.getPassword());
    }

    return Redisson.create(config);
  }

  /**
   * 云托管模式
   * 
   * @param redissonProperties
   * @return client
   */
  private RedissonClient redissonReplicatedServers(RedissonProperties redissonProperties) {
    Config config = new Config();
    ReplicatedServersConfig serverConfig =
        config.useReplicatedServers().addNodeAddress(redissonProperties.getNodeAddresses())
            .setTimeout(redissonProperties.getTimeout())
            .setScanInterval(redissonProperties.getScanInterval())
            .setDnsMonitoringInterval(redissonProperties.getDnsMonitoringInterval())
            .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize())
            .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize());

    if (redissonProperties.getPassword() != null && redissonProperties.getPassword().length() > 0) {
      serverConfig.setPassword(redissonProperties.getPassword());
    }

    return Redisson.create(config);
  }

  /**
   * 哨兵模式
   * 
   * @param redissonProperties
   * @return client
   */
  private RedissonClient redissonuseSentinelServers(RedissonProperties redissonProperties) {
    Config config = new Config();
    SentinelServersConfig serverConfig =
        config.useSentinelServers().addSentinelAddress(redissonProperties.getSentinelAddresses())
            .setTimeout(redissonProperties.getTimeout())
            .setScanInterval(redissonProperties.getScanInterval())
            .setMasterName(redissonProperties.getMasterName())
            .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize())
            .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize());
    if (redissonProperties.getPassword() != null && redissonProperties.getPassword().length() > 0) {
      serverConfig.setPassword(redissonProperties.getPassword());
    }


    return Redisson.create(config);
  }

  /**
   * 主从模式
   * 
   * @param redissonProperties
   * @return client
   */
  private RedissonClient redissonuseMasterSlaveServers(RedissonProperties redissonProperties) {
    Config config = new Config();
    MasterSlaveServersConfig serverConfig =
        config.useMasterSlaveServers().setMasterAddress(redissonProperties.getMasterAddress())
            .addSlaveAddress(redissonProperties.getSlaveAddresses())
            .setTimeout(redissonProperties.getTimeout())
            .setDnsMonitoringInterval(redissonProperties.getDnsMonitoringInterval())
            .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize())
            .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize());

    if (redissonProperties.getPassword() != null && redissonProperties.getPassword().length() > 0) {
      serverConfig.setPassword(redissonProperties.getPassword());
    }

    return Redisson.create(config);
  }

}
