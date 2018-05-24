package vip.justlive.frost.core.monitor;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.Maps;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.notify.EventPublisher;
import vip.justlive.frost.core.persistence.JobRepository;

/**
 * 超时监听
 * 
 * @author wubo
 *
 */
@Component("timeoutMonitorImpl")
public class TimeoutMonitorImpl implements Monitor {

  private static final String MESSAGE_TEMPLATE = "The job [%s(%s)] execution exceeds %s seconds";

  @Autowired
  JobRepository jobRepository;

  @Autowired
  EventPublisher publisher;

  ScheduledExecutorService executorService =
      new ScheduledThreadPoolExecutor(10, new BasicThreadFactory.Builder()
          .namingPattern("timeout-monitor-pool-%d").daemon(true).build());

  Map<String, ScheduledFuture<?>> futureMap = Maps.newConcurrentMap();

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
