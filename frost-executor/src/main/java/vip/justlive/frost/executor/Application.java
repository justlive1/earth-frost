package vip.justlive.frost.executor;

import vip.justlive.frost.core.config.JobConfig;

/**
 * 启动器
 * 
 * @author wubo
 *
 */
public class Application {

  public static void main(String[] args) {
    JobConfig.initExecutor();
  }
}
