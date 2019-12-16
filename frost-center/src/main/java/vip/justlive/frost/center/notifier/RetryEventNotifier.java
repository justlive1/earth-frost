package vip.justlive.frost.center.notifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.notify.Event;

/**
 * 失败重试事件通知器
 *
 * @author wubo
 */
public class RetryEventNotifier extends AbstractEventNotifier {

  private static final List<String> SUPPORT_EVENTS =
      Arrays.asList(Event.TYPE.DISPATCH_FAIL_RETRY.name(), Event.TYPE.EXECUTE_FAIL_RETRY.name());

  @Override
  protected boolean shouldNotify(Event event) {
    JobInfo jobInfo = Container.get().getJobRepository()
        .findJobInfoById(event.getData().getJobId());
    if (jobInfo == null) {
      return false;
    }
    return SUPPORT_EVENTS.contains(event.getType())
        && Objects.equals(JobInfo.STRATEGY.RETRY.name(), jobInfo.getFailStrategy());
  }

  @Override
  protected void doNotify(Event event) {
    if (Objects.equals(Event.TYPE.EXECUTE_FAIL_RETRY.name(), event.getType())) {
      Container.get().getDispatcher().dispatch(event.getData());
    } else if (Objects.equals(Event.TYPE.DISPATCH_FAIL_RETRY.name(), event.getType())) {
      JobExecuteParam param = event.getData();
      Container.get().getJobSchedule().retryJob(param.getJobId(), param.getLoggerId(),
          param.getParentLoggerId());
    }
  }

}
