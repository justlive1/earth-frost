package com.example.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.oxygen.core.Bootstrap;

@SpringBootApplication
public class Application4Spring {

  public static void main(String[] args) {
    // 初始化job配置文件 因为job未使用spring，需要指定配置文件
    Bootstrap.initConfig("classpath:config/*.properties");
    SpringApplication.run(Application4Spring.class, args);
    // 注册job
    JobConfig.initExecutor();
  }
}
