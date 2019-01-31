package vip.justlive.frost.core.config;

import com.google.common.collect.Maps;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.frost.core.job.Job;
import vip.justlive.frost.core.monitor.TimeoutMonitorImpl;
import vip.justlive.frost.core.registry.RedisRegistry;
import vip.justlive.frost.core.registry.Registry;
import vip.justlive.frost.core.util.IpUtils;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.ValueConfig;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.ioc.BeanStore;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * 执行器配置属性
 *
 * @author wubo
 */
@Slf4j
public class JobConfig {


  public static final String TASK_ID = "frost:taskid";
  public static final String WORKER = "frost:workers";
  public static final String WORKER_REGISTER = "frost:workers:register";
  public static final String WORKER_REQ = "frost:workers:req:%s";
  public static final String WORKER_REQ_VAL = "frost:workers:reqval:%s";

  public static final String JOB_BEAN_CHANNEL = "frost:job:bean:%s:%s";
  public static final String JOB_SCRIPT_CHANNEL = "frost:job:script:%s";
  public static final String JOB_INFO = "frost:jobinfo";
  public static final String JOB_INFO_SORT = "frost:jobinfo:sort";
  public static final String JOB_INFO_SCRIPT = "frost:jobinfo:script";

  public static final String RECORD = "frost:record";
  public static final String RECORD_SORT = "frost:record:sort";
  public static final String RECORD_STATUS = "frost:record:status";

  public static final String EVENT = "frost:event";
  public static final String EVENT_SHARDING = "frost:event:sharding:%s:%s";

  public static final String LOG_BIND = "frost:logger:bind";
  public static final String LOG_REL = "frost:logger:rel";

  public static final String STAT_TOTAL_TYPE = "frost:stat:total:%s";
  public static final String STAT_TOTAL_RUNNING = "frost:stat:total:running";
  public static final String STAT_DATE_TYPE_SUCCESS = "frost:stat:%s:%s:success";
  public static final String STAT_DATE_TYPE_FAIL = "frost:stat:%s:%s:fail";
  public static final String STAT_TYPE_DISPATCH = "dispatch";
  public static final String STAT_TYPE_EXECUTE = "execute";

  private static final Map<String, BaseJob> JOBS = Maps.newConcurrentMap();
  private static final List<JobGroup> JOB_GROUPS = new LinkedList<>();

  private JobConfig() {
  }

  /**
   * 初始化executor
   */
  public static void initExecutor() {
    Bootstrap.start();
    // monitor
    BeanStore.putBean(TimeoutMonitorImpl.class.getName(), new TimeoutMonitorImpl());
    // registry
    Registry registry = new RedisRegistry();
    registry.register();
  }

  /**
   * 添加job
   *
   * @param job job
   */
  public static void addJob(BaseJob job) {
    if (job.getClass().isAnnotationPresent(Job.class)) {
      Job jobAnnotation = job.getClass().getAnnotation(Job.class);
      if (JOBS.putIfAbsent(jobAnnotation.value(), job) == null) {
        JobGroup jobGroup = new JobGroup();
        // id
        jobGroup.setId(jobAnnotation.value());
        jobGroup.setJobKey(jobAnnotation.value());
        jobGroup.setJobDesc(jobAnnotation.desc());
        jobGroup.setGroupKey(getExecutor().key);
        JOB_GROUPS.add(jobGroup);
      } else {
        BaseJob exist = JOBS.get(jobAnnotation.value());
        if (exist == job) {
          return;
        }
        if (ClassUtils.getCglibActualClass(job.getClass()) == ClassUtils
            .getCglibActualClass(exist.getClass())) {
          log.warn("There is already a job [{}] and the job [{}] will be ignored.", exist, job);
          return;
        }
        throw Exceptions.fail("30000", String.format("job [%s] 已存在", jobAnnotation.value()));
      }
    }
  }

  /**
   * 获取job
   *
   * @param key key
   * @return job
   */
  public static BaseJob findJob(String key) {
    return JOBS.get(key);
  }

  /**
   * 获取jobExecutor
   *
   * @return JobExecutor
   */
  public static JobExecutor getJobExecutor() {
    JobExecutor jobExecutor = new JobExecutor();
    jobExecutor.setId(UUID.randomUUID().toString());
    jobExecutor.setName(JobConfig.getExecutor().getName());
    jobExecutor.setKey(JobConfig.getExecutor().getKey());

    String address = JobConfig.getExecutor().getIp();
    if (address == null || address.length() == 0) {
      address = IpUtils.ip();
    }
    address += IpUtils.SEPERATOR + JobConfig.getExecutor().getPort();
    jobExecutor.setAddress(address);
    jobExecutor.setGroups(JOB_GROUPS);
    return jobExecutor;
  }

  /**
   * 每个job支持并行处理数
   */
  public static int getParallel() {
    return Integer.parseInt(ConfigFactory.getProperty("frost.job.parallel", "1"));
  }

  /**
   * 执行器
   */
  public static Executor getExecutor() {
    return ConfigFactory.load(Executor.class);
  }

  @Data
  @ValueConfig("frost.job.executor")
  public static class Executor {

    /**
     * 执行器名称
     */
    private String name;

    /**
     * 执行器Key
     */
    private String key;

    /**
     * 执行器部署ip
     */
    private String ip;

    /**
     * 执行器监听端口
     */
    private Integer port;

    /**
     * 是否开启脚本任务执行
     */
    private Boolean scriptJobEnabled = true;

    /**
     * 错过执行的阈值(毫秒)
     */
    private long misfireThreshold = 5000L;

    /**
     * 最大日志数
     */
    private long maxLogSize = -1;
  }

}
