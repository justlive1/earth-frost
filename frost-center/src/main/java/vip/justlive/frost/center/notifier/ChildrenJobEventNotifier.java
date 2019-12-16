package vip.justlive.frost.center.notifier;

import java.util.Objects;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobSharding;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.notify.Event;

/**
 * 子任务事件通知器
 *
 * @author wubo
 */
public class ChildrenJobEventNotifier extends AbstractEventNotifier {

  @Override
  protected boolean shouldNotify(Event event) {
    JobInfo jobInfo =
        Container.get().getJobRepository().findJobInfoById(event.getData().getJobId());
    if (jobInfo != null && Objects.equals(event.getType(), Event.TYPE.EXECUTE_SUCCESS.name())
        && jobInfo.getChildJobIds() != null && jobInfo.getChildJobIds().length > 0) {
      JobSharding sharding = event.getData().getSharding();
      if (sharding == null) {
        return true;
      }
      int count = (int) Container.get().getRedissonClient().getAtomicLong(String
          .format(Container.EVENT_SHARDING, event.getData().getJobId(),
              event.getData().getLoggerId())).incrementAndGet();
      return count == sharding.getTotal();
    }
    return false;
  }

  @Override
  protected void doNotify(Event event) {
    JobInfo jobInfo = Container.get().getJobRepository()
        .findJobInfoById(event.getData().getJobId());
    if (jobInfo.getChildJobIds() != null) {
      for (String jobId : jobInfo.getChildJobIds()) {
        Container.get().getJobSchedule().triggerChildJob(jobId, event.getData().getLoggerId());
      }
    }
  }

}
