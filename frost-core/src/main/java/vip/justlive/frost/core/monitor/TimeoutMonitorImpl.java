package vip.justlive.frost.core.monitor;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Maps;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.notify.EventPublisher;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * 超时监听
 * 
 * @author wubo
 *
 */
public class TimeoutMonitorImpl implements Monitor {

  private static final String MESSAGE_TEMPLATE = "The job [%s(%s)] execution exceeds %s seconds";

  private final JobRepository jobRepository;
  private final EventPublisher publisher;
  private final ScheduledExecutorService executorService;
  private final Map<String, ScheduledFuture<?>> futureMap;

  public TimeoutMonitorImpl() {
    this.jobRepository = BeanStore.getBean(JobRepository.class);
    this.publisher = BeanStore.getBean(EventPublisher.class);
    this.executorService = ThreadUtils.newScheduledExecutor(10, "timeout-monitor");
    this.futureMap = Maps.newConcurrentMap();
  }

  @Override
  public void watch(JobExecuteParam target) {

    JobInfo jobInfo = jobRepository.findJobInfoById(target.getJobId());
    if (jobInfo == null || jobInfo.getTimeout() == null || jobInfo.getTimeout() <= 0) {
      return;
    }

    String message =
        String.format(MESSAGE_TEMPLATE, jobInfo.getName(), jobInfo.getId(), jobInfo.getTimeout());
    ScheduledFuture<?> future = executorService.schedule(new Commond(target, message),
        jobInfo.getTimeout(), TimeUnit.SECONDS);

    futureMap.put(target.getLoggerId(), future);
  }

  @Override
  public void unWatch(JobExecuteParam target) {

    ScheduledFuture<?> future = futureMap.remove(target.getLoggerId());
    if (future != null) {
      future.cancel(true);
    }
  }

  class Commond implements Runnable {

    JobExecuteParam data;
    String message;

    public Commond(JobExecuteParam data, String message) {
      this.data = data;
      this.message = message;
    }

    @Override
    public void run() {
      publisher.publish(
          new Event(data, Event.TYPE.TIMEOUT_MONITOR.name(), message, System.currentTimeMillis()));
    }

  }

}
