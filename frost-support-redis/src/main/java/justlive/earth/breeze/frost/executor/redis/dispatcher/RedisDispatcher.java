package justlive.earth.breeze.frost.executor.redis.dispatcher;

import java.lang.reflect.Field;
import org.redisson.RedissonExecutorService;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import justlive.earth.breeze.frost.core.dispacher.Dispatcher;
import justlive.earth.breeze.frost.core.executor.JobWrapper;
import justlive.earth.breeze.frost.core.model.JobGroup;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;
import justlive.earth.breeze.snow.common.base.exception.Exceptions;
import justlive.earth.breeze.snow.common.base.util.Checks;

/**
 * redis分发实现类
 * 
 * @author wubo
 *
 */
@Profile("center")
@Component
public class RedisDispatcher implements Dispatcher {

  @Autowired
  RedissonClient redissonClient;

  @Override
  public void dispatch(JobInfo job) {

    String key = this.checkDispatch(job);
    redissonClient.getExecutorService(key).execute(new JobWrapper(job));
  }

  @Override
  public String checkDispatch(JobInfo job) {

    JobGroup jobGroup = Checks.notNull(Checks.notNull(job).getGroup());
    String key = String.join(SystemProperties.SEPERATOR, SystemProperties.JOB_GROUP_PREFIX,
        jobGroup.getGroupKey(), jobGroup.getJobKey());

    // redisson 当没有worker时候，调用countActiveWorkers会阻塞
    // 由于计算count是基于订阅模式下的publish触发增加各自worker到workersCounterAtomicLong事件
    // 再去获取semaphore，最后返回workersCounterAtomicLong的数值
    // 但是没有worker的时候，publish返回0， 触发不了事件，导致semaphore阻塞
    // Semaphore阻塞的情况：Semaphore第一次getSemaphore且acquire(0)会阻塞
    // 这个问题已经提到redisson的issue上
    if (nonActiveWorkers(key)) {
      throw Exceptions.fail("30000", "没有可调度的执行器");
    }
    int workers = redissonClient.getExecutorService(key).countActiveWorkers();
    if (workers == 0) {
      throw Exceptions.fail("30000", "没有可调度的执行器");
    }
    return key;
  }

  /**
   * remove this method redisson fixed the bug <br>
   * 注意：这个方法没有清除用于计算的semaphore和counter
   */
  private boolean nonActiveWorkers(String key) {
    RScheduledExecutorService service = redissonClient.getExecutorService(key);
    Field field = ReflectionUtils.findField(RedissonExecutorService.class, "workersTopic");
    try {
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      RTopic<String> workTopic = (RTopic<String>) field.get(service);
      int count = (int) workTopic.publish("0");
      return count == 0;
    } catch (IllegalArgumentException | IllegalAccessException e) {
      // nothing
      return false;
    }
  }
}
