package vip.justlive.frost.client;

import lombok.Getter;
import vip.justlive.frost.api.facade.JobApiFacade;
import vip.justlive.oxygen.core.config.ConfigFactory;

/**
 * 代理类
 * 
 * @author wubo
 *
 */
@Getter
public class FacadeProxy {

  private JobApiFacade jobApiFacade;

  FacadeProxy(String... location) {
    ConfigFactory.loadProperties(location);
    ClientProperties clientProps = ConfigFactory.load(ClientProperties.class);
    this.jobApiFacade = new JobApiFacadeImpl(clientProps);
  }

  /**
   * 创建代理
   * 
   * @param location 配置文件路径
   * @return
   */
  public static FacadeProxy newProxy(String... location) {
    return new FacadeProxy(location);
  }

}
