package vip.justlive.frost.center.notifier;

import java.util.Objects;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.job.JobSchedule;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * 子任务事件通知器
 * 
 * @author wubo
 *
 */
public class ChildrenJobEventNotifier extends AbstractEventNotifier {

  @Override
  protected boolean shouldNotify(Event event) {
    JobInfo jobInfo =
        BeanStore.getBean(JobRepository.class).findJobInfoById(event.getData().getJobId());
    return jobInfo != null && Objects.equals(event.getType(), Event.TYPE.EXECUTE_SUCCESS.name())
        && jobInfo.getChildJobIds() != null && jobInfo.getChildJobIds().length > 0;
  }

  @Override
  protected void doNotify(Event event) {
    JobInfo jobInfo =
        BeanStore.getBean(JobRepository.class).findJobInfoById(event.getData().getJobId());
    if (jobInfo.getChildJobIds() != null) {
      for (String jobId : jobInfo.getChildJobIds()) {
        BeanStore.getBean(JobSchedule.class).triggerChildJob(jobId, event.getData().getLoggerId());
      }
    }
  }

}
