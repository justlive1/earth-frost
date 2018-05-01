package justlive.earth.breeze.frost.executor.redis.config;

import java.util.Map;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSender;
import justlive.earth.breeze.frost.core.notify.CompositeNotifier;
import justlive.earth.breeze.frost.core.notify.EventListener;
import justlive.earth.breeze.frost.core.notify.EventPublisher;
import justlive.earth.breeze.frost.core.notify.MailEventNotifier;
import justlive.earth.breeze.frost.core.notify.Notifier;
import justlive.earth.breeze.frost.executor.redis.notify.RedisEventPublisherImpl;

/**
 * 通知配置
 * 
 * @author wubo
 *
 */
@Configuration
public class NotifierConfig {


  @Bean
  EventPublisher publisher() {

    return new RedisEventPublisherImpl();
  }


  @Bean
  @Profile(SystemProperties.PROFILE_CENTER)
  @ConditionalOnProperty(value = "frost.notifier.mail.enabled", havingValue = "true",
      matchIfMissing = true)
  @ConfigurationProperties("frost.notifier.mail")
  MailEventNotifier mailEventNotifier(MailSender sender) {
    return new MailEventNotifier(sender);
  }

  @Bean
  @Profile(SystemProperties.PROFILE_CENTER)
  @ConditionalOnBean(Notifier.class)
  CompositeNotifier compositeNotifier(Map<String, Notifier> notifiers) {
    return new CompositeNotifier(notifiers.values());
  }

  @Bean
  @Profile(SystemProperties.PROFILE_CENTER)
  @ConditionalOnBean(CompositeNotifier.class)
  EventListener listener(CompositeNotifier notifier, SystemProperties props,
      RedissonClient redissonClient) {
    RScheduledExecutorService executor =
        redissonClient.getExecutorService(String.join(SystemProperties.SEPERATOR,
            SystemProperties.CENTER_PREFIX, EventPublisher.class.getName()));
    executor.registerWorkers(props.getWorkers());

    return new EventListener(notifier);
  }
}
