package vip.justlive.frost.center.notifier;

import java.util.Objects;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobRecordStatus;
import vip.justlive.frost.api.model.JobSharding;
import vip.justlive.frost.core.config.JobConfig;
import vip.justlive.frost.core.job.JobSchedule;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * 子任务事件通知器
 *
 * @author wubo
 */
public class ChildrenJobEventNotifier extends AbstractEventNotifier {

  @Override
  protected boolean shouldNotify(Event event) {
    JobInfo jobInfo =
        BeanStore.getBean(JobRepository.class).findJobInfoById(event.getData().getJobId());
    if (jobInfo != null && Objects.equals(event.getType(), Event.TYPE.EXECUTE_SUCCESS.name())
        && jobInfo.getChildJobIds() != null && jobInfo.getChildJobIds().length > 0) {
      JobSharding sharding = event.getData().getSharding();
      if (sharding == null) {
        return true;
      }
      long count = BeanStore.getBean(RedissonClient.class).<String, JobRecordStatus>getListMultimap(
          JobConfig.RECORD_STATUS).getAll(event.getData().getLoggerId()).stream().filter(
          r -> JobExecuteRecord.STATUS.SUCCESS.name().equals(r.getStatus()) && (r.getType() == 1
              || r.getType() == 3)).count();
      return count == sharding.getTotal();
    }
    return false;
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
