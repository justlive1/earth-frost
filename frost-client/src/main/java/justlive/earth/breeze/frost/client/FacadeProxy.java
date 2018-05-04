package justlive.earth.breeze.frost.client;

import justlive.earth.breeze.frost.api.facade.JobApiFacade;
import justlive.earth.breeze.snow.common.base.support.ConfigFactory;
import lombok.Getter;

@Getter
public class FacadeProxy {

  private JobApiFacade jobApiFacade;

  FacadeProxy(String location) {
    ConfigFactory.loadProperties(location);
    ClientProperties clientProps = ConfigFactory.load(ClientProperties.class);
    this.jobApiFacade = new JobApiFacadeImpl(clientProps);
  }

  public static FacadeProxy newProxy(String location) {
    return new FacadeProxy(location);
  }

}
