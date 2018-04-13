package justlive.earth.breeze.frost.core.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import justlive.earth.breeze.frost.core.config.ExecutorProperties;
import justlive.earth.breeze.frost.core.job.IJob;
import justlive.earth.breeze.frost.core.job.Job;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobGroup;
import justlive.earth.breeze.frost.core.util.IpUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册抽象类
 * 
 * @author wubo
 *
 */
@Slf4j
public abstract class AbstractRegistry implements Registry {

  @Autowired
  protected ExecutorProperties executorProps;

  @Autowired
  protected List<IJob> jobs;

  protected JobExecutor jobExecutorBean;

  @PostConstruct
  protected void init() {
    jobExecutorBean = this.jobExecutor();
  }

  /**
   * 当前执行器
   * 
   * @return
   */
  protected JobExecutor jobExecutor() {
    JobExecutor jobExecutor = new JobExecutor();
    jobExecutor.setId(UUID.randomUUID().toString());
    jobExecutor.setName(executorProps.getName());
    jobExecutor.setKey(executorProps.getKey());

    String address = executorProps.getIp();
    if (!StringUtils.hasText(address)) {
      address = IpUtils.ip();
    }
    address += IpUtils.SEPERATOR + executorProps.getPort();
    jobExecutor.setAddress(address);
    jobExecutor.setGroups(this.jobGroups(executorProps.getKey(), executorProps.getName()));
    return jobExecutor;
  }

  /**
   * job分组
   * 
   * @return
   */
  protected List<JobGroup> jobGroups(String groupKey, String groupDesc) {
    List<JobGroup> list = new ArrayList<>();
    for (IJob job : jobs) {
      if (job.getClass().isAnnotationPresent(Job.class)) {
        Job jobAnnotation = job.getClass().getAnnotation(Job.class);
        JobGroup jobGroup = new JobGroup();
        // id
        jobGroup.setId(jobAnnotation.value());
        jobGroup.setJobKey(jobAnnotation.value());
        jobGroup.setJobDesc(jobAnnotation.desc());
        jobGroup.setGroupKey(groupKey);
        jobGroup.setGroupDesc(groupDesc);
        list.add(jobGroup);
      } else {
        log.warn("[{}] missing @Job", job.getClass());
      }
    }
    return list;
  }
}
