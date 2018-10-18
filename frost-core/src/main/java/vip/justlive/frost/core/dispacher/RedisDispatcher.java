package vip.justlive.frost.core.dispacher;

import java.util.Objects;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecuteParam;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.job.JobBeanExecuteWrapper;
import vip.justlive.frost.core.job.JobScriptExecuteWrapper;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * redis分发实现类
 * 
 * @author wubo
 *
 */
public class RedisDispatcher implements Dispatcher {

  private final RedissonClient redissonClient;
  private final JobRepository jobRepository;

  public RedisDispatcher(RedissonClient redissonClient, JobRepository jobRepository) {
    this.redissonClient = redissonClient;
    this.jobRepository = jobRepository;
  }

  @Override
  public void dispatch(JobExecuteParam param) {

    JobInfo jobInfo = jobRepository.findJobInfoById(param.getJobId());
    if (jobInfo == null) {
      throw Exceptions.fail("30005", String.format("未查询到任务 %s", param));
    }
    this.checkDispatch(param);
    if (Objects.equals(JobInfo.TYPE.SCRIPT.name(), jobInfo.getType())) {
      redissonClient.getExecutorService(param.getTopicKey())
          .execute(new JobScriptExecuteWrapper(param));
    } else {
      redissonClient.getExecutorService(param.getTopicKey())
          .execute(new JobBeanExecuteWrapper(param));
    }
  }

  @Override
  public void checkDispatch(JobExecuteParam param) {
    count(param);
  }

  @Override
  public int count(JobExecuteParam param) {
    int workers = redissonClient.getExecutorService(param.getTopicKey()).countActiveWorkers();
    if (workers == 0) {
      throw Exceptions.fail("30000", "没有可调度的执行器");
    }
    return workers;
  }

}
