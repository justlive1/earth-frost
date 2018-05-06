package justlive.earth.breeze.frost.core.notify;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import justlive.earth.breeze.frost.api.model.JobExecuteParam;
import justlive.earth.breeze.frost.api.model.JobInfo;
import justlive.earth.breeze.frost.core.dispacher.Dispatcher;
import justlive.earth.breeze.frost.core.job.JobSchedule;

/**
 * 失败重试事件通知器
 * 
 * @author wubo
 *
 */
public class RetryEventNotifier extends AbstractEventNotifier {

  private static final List<String> SUPPORT_EVENTS =
      Arrays.asList(Event.TYPE.DISPATCH_FAIL_RETRY.name(), Event.TYPE.EXECUTE_FAIL_RETRY.name());

  @Autowired
  JobSchedule jobSchedule;

  @Autowired
  Dispatcher dispatcher;


  @Override
  protected boolean shouldNotify(Event event) {
    return SUPPORT_EVENTS.contains(event.getType())
        && Objects.equals(JobInfo.STRATEGY.RETRY.name(), event.getData().getFailStrategy());
  }

  @Override
  protected void doNotify(Event event) {
    if (Objects.equals(Event.TYPE.EXECUTE_FAIL_RETRY.name(), event.getType())) {
      dispatcher.dispatch(event.getData());
    } else if (Objects.equals(Event.TYPE.DISPATCH_FAIL_RETRY.name(), event.getType())) {
      JobExecuteParam param = event.getData();
      jobSchedule.retryJob(param.getJobId(), param.getLoggerId());
    }
  }

}
