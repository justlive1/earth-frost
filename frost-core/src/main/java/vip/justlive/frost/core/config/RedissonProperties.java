package vip.justlive.frost.core.config;

import lombok.Data;
import vip.justlive.oxygen.core.config.ValueConfig;

/**
 * redis配置属性
 * 
 * @author wubo
 *
 */
@Data
@ValueConfig("redisson")
public class RedissonProperties {

  /**
   * 模式
   */
  private int mode = 0;
  /**
   * 超时时间
   */
  private Integer timeout = 3000;

  /**
   * 地址
   */
  private String address;

  /**
   * 密码
   */
  private String password;

  /**
   * 连接池
   */
  private Integer connectionPoolSize = 64;

  /**
   * 连接空闲
   */
  private Integer connectionMinimumIdleSize = 10;

  /**
   * slave连接数
   */
  private int slaveConnectionPoolSize = 250;

  /**
   * master连接数
   */
  private int masterConnectionPoolSize = 250;

  /**
   * 集群地址
   */
  private String[] nodeAddresses;

  /**
   * 集群模式扫描间隔
   */
  private Integer scanInterval = 2000;

  /**
   * DNS监控间隔
   */
  private Integer dnsMonitoringInterval = 5000;

  /**
   * 哨兵节点地址
   */
  private String[] sentinelAddresses;

  /**
   * 主服务器的名称
   */
  private String masterName;

  /**
   * 主节点地址
   */
  private String masterAddress;

  /**
   * 从主节点地址
   */
  private String[] slaveAddresses;

}
