package justlive.earth.breeze.frost.client;

import justlive.earth.breeze.snow.common.base.annotation.Value;
import lombok.Data;

/**
 * 
 * @author wubo
 *
 */
@Data
public class ClientProperties {

  @Value("${frost.client.baseUrl}")
  private String baseUrl;

  @Value("${frost.client.username}")
  private String username;

  @Value("${frost.client.password}")
  private String password;
}
