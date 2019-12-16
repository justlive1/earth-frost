package vip.justlive.frost.center.config;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import vip.justlive.frost.center.notifier.ChildrenJobEventNotifier;
import vip.justlive.frost.center.notifier.CompositeNotifier;
import vip.justlive.frost.center.notifier.DingtalkEventNotifier;
import vip.justlive.frost.center.notifier.MailEventNotifier;
import vip.justlive.frost.center.notifier.RetryEventNotifier;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.config.RedissonProperties;
import vip.justlive.frost.core.config.SystemProperties;
import vip.justlive.frost.core.notify.Notifier;

/**
 * 通知配置
 *
 * @author wubo
 */
@Configuration
public class CenterConfig {

  @Bean
  @ConditionalOnProperty(value = "frost.notifier.mail.enabled", havingValue = "true", matchIfMissing = true)
  @ConfigurationProperties("frost.notifier.mail")
  public MailEventNotifier mailEventNotifier(MailSender sender) {
    return new MailEventNotifier(sender);
  }

  @Bean
  @ConditionalOnProperty(value = "frost.notifier.dingtalk.enabled", havingValue = "true", matchIfMissing = true)
  @ConfigurationProperties("frost.notifier.dingtalk")
  public DingtalkEventNotifier dingtalkEventNotifier() {
    return new DingtalkEventNotifier();
  }

  @Bean
  public RetryEventNotifier retryEventNotifier() {
    return new RetryEventNotifier();
  }

  @Bean
  public ChildrenJobEventNotifier childrenJobEventNotifier() {
    return new ChildrenJobEventNotifier();
  }

  @Bean
  @ConditionalOnBean(Notifier.class)
  public CompositeNotifier compositeNotifier(Map<String, Notifier> notifiers) {
    return new CompositeNotifier(notifiers.values());
  }

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
  public Container container(CompositeNotifier notifier, SystemProperties systemProperties,
      RedissonProperties redissonProperties) {
    Container.initCenter(redissonProperties, systemProperties, notifier);
    return Container.get();
  }

}
