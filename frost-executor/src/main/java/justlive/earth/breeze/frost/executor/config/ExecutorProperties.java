package justlive.earth.breeze.frost.executor.config;

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

  private String name;
  
  private String key;

  private String ip;

  private Integer port;

}
