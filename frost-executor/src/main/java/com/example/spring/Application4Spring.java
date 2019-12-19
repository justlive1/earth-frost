package com.example.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.config.JobExecutorProperties;
import vip.justlive.frost.core.config.RedissonProperties;
import vip.justlive.frost.core.config.SystemProperties;

/**
 * @author wubo
 */
@SpringBootApplication
public class Application4Spring {

  public static void main(String[] args) {
    SpringApplication.run(Application4Spring.class, args);
  }

  @Configuration
  static class Config {

    @Bean
    @ConfigurationProperties("frost.system")
    public SystemProperties systemProperties() {
      return new SystemProperties();
    }

    @Bean
    @ConfigurationProperties("frost.redisson")
    public RedissonProperties redissonProperties() {
      return new RedissonProperties();
    }

    @Bean
    @ConfigurationProperties("frost.executor")
    public JobExecutorProperties jobExecutorProperties() {
      return new JobExecutorProperties();
    }

    @Bean
    public Container container(SystemProperties systemProperties,
        RedissonProperties redissonProperties, JobExecutorProperties jobExecutorProperties) {
      Container.initExecutor(jobExecutorProperties, redissonProperties, systemProperties);
      return Container.get();
    }
  }
}
