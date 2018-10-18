package vip.justlive.frost.center.config;

import java.util.Map;
import javax.annotation.PostConstruct;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
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
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.frost.core.config.SystemProperties;
import vip.justlive.frost.core.dispacher.Dispatcher;
import vip.justlive.frost.core.dispacher.RedisDispatcher;
import vip.justlive.frost.core.job.RedisJobScheduleImpl;
import vip.justlive.frost.core.notify.EventListener;
import vip.justlive.frost.core.notify.EventPublisher;
import vip.justlive.frost.core.notify.Notifier;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * 通知配置
 * 
 * @author wubo
 *
 */
@Configuration
public class CenterConfig {

  @Bean
  @ConditionalOnProperty(value = "frost.notifier.mail.enabled", havingValue = "true",
      matchIfMissing = true)
  @ConfigurationProperties("frost.notifier.mail")
  MailEventNotifier mailEventNotifier(MailSender sender) {
    return new MailEventNotifier(sender);
  }

  @Bean
  @ConditionalOnProperty(value = "frost.notifier.dingtalk.enabled", havingValue = "true",
      matchIfMissing = true)
  @ConfigurationProperties("frost.notifier.dingtalk")
  DingtalkEventNotifier dingtalkEventNotifier() {
    return new DingtalkEventNotifier();
  }

  @Bean
  RetryEventNotifier retryEventNotifier() {
    return new RetryEventNotifier();
  }

  @Bean
  ChildrenJobEventNotifier childrenJobEventNotifier() {
    return new ChildrenJobEventNotifier();
  }

  @Bean
  @ConditionalOnBean(Notifier.class)
  CompositeNotifier compositeNotifier(Map<String, Notifier> notifiers) {
    return new CompositeNotifier(notifiers.values());
  }

  @Autowired
  CompositeNotifier notifier;

  @PostConstruct
  public void initCenter() {
    Bootstrap.start();
    RedissonClient redissonClient = BeanStore.getBean(RedissonClient.class);
    JobRepository jobRepository = BeanStore.getBean(JobRepository.class);
    // schedule
    BeanStore.putBean(RedisJobScheduleImpl.class.getName(), new RedisJobScheduleImpl());
    // dispatcher
    Dispatcher dispatcher = new RedisDispatcher(redissonClient, jobRepository);
    BeanStore.putBean(RedisDispatcher.class.getName(), dispatcher);
    RScheduledExecutorService executor = redissonClient.getExecutorService(
        String.join(JobConfig.SEPERATOR, JobConfig.CENTER_PREFIX, EventPublisher.class.getName()));
    executor.registerWorkers(ConfigFactory.load(SystemProperties.class).getWorkers());
    EventListener listener = new EventListener(notifier);
    BeanStore.putBean(EventListener.class.getName(), listener);
  }
}
