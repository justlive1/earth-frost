package vip.justlive.frost.executor;

import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.config.JobExecutorProperties;
import vip.justlive.frost.core.config.RedissonProperties;
import vip.justlive.frost.core.config.SystemProperties;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.config.ConfigFactory;

/**
 * 启动器
 *
 * @author wubo
 */
public class Application {

  public static void main(String[] args) {
    Bootstrap.start();
    Container.initExecutor(ConfigFactory.load(JobExecutorProperties.class),
        ConfigFactory.load(RedissonProperties.class), ConfigFactory.load(SystemProperties.class));
  }
}
