package justlive.earth.breeze.frost.executor.redis.dispatcher;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import justlive.earth.breeze.frost.core.dispacher.Dispatcher;
import justlive.earth.breeze.frost.core.executor.JobWrapper;
import justlive.earth.breeze.frost.core.model.JobGroup;
import justlive.earth.breeze.frost.core.model.JobInfo;
import justlive.earth.breeze.frost.executor.redis.config.SystemProperties;
import justlive.earth.breeze.snow.common.base.exception.Exceptions;
import justlive.earth.breeze.snow.common.base.util.Checks;

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
    int workers = redissonClient.getExecutorService(key).countActiveWorkers();
    if (workers == 0) {
      throw Exceptions.fail("30000", "没有可调度的执行器");
    }
    return key;
  }

}
