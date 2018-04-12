package justlive.earth.breeze.frost.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * 执行器配置属性
 * 
 * @author wubo
 */
@Data
@ConfigurationProperties(prefix = "executor")
@Configuration
public class ExecutorProperties {

  /**
   * 执行器名称
   */
  private String name;

  /**
   * 执行器Key
   */
  private String key;

  /**
   * 执行器部署ip
   */
  private String ip;

  /**
   * 执行器监听端口
   */
  private Integer port;

  /**
   * 每个job支持并行处理数
   */
  private Integer parallel = 1;

}
