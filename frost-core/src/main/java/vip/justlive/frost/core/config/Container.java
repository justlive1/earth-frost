package vip.justlive.frost.core.config;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.core.dispacher.Dispatcher;
import vip.justlive.frost.core.dispacher.RedisDispatcher;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.frost.core.job.Job;
import vip.justlive.frost.core.job.JobLogger;
import vip.justlive.frost.core.job.JobSchedule;
import vip.justlive.frost.core.job.RedisJobLoggerImpl;
import vip.justlive.frost.core.job.RedisJobScheduleImpl;
import vip.justlive.frost.core.monitor.Monitor;
import vip.justlive.frost.core.monitor.TimeoutMonitorImpl;
import vip.justlive.frost.core.notify.EventListener;
import vip.justlive.frost.core.notify.EventPublisher;
import vip.justlive.frost.core.notify.Notifier;
import vip.justlive.frost.core.notify.RedisEventPublisherImpl;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.frost.core.persistence.RedisJobRepositoryImpl;
import vip.justlive.frost.core.registry.RedisRegistry;
import vip.justlive.frost.core.registry.Registry;
import vip.justlive.frost.core.service.JobService;
import vip.justlive.frost.core.service.RedisJobServiceImpl;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.MoreObjects;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.oxygen.core.util.SystemUtils;

/**
 * 执行器配置属性
 *
 * @author wubo
 */
@Slf4j
@Getter
public class Container {

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

  private static final Map<String, BaseJob> JOBS = new ConcurrentHashMap<>();
  private static final List<JobGroup> JOB_GROUPS = new LinkedList<>();
  private static Container CT;

  private SystemProperties systemProperties;
  private JobExecutorProperties jobExecutorProperties;
  private RedissonClient redissonClient;
  private Dispatcher dispatcher;
  private Registry registry;
  private JobRepository jobRepository;
  private JobService jobService;
  private JobSchedule jobSchedule;
  private JobLogger jobLogger;
  private EventPublisher publisher;
  private Monitor monitor;
  private EventListener listener;
  private JobExecutor jobExecutor;

  /**
   * 获取容器
   *
   * @return container
   */
  public static Container get() {
    return MoreObjects.notNull(CT);
  }

  /**
   * 初始化executor
   *
   * @param jobExecutorProperties 执行器配置
   * @param redissonProperties redisson配置
   * @param systemProperties 系统配置
   */
  public static void initExecutor(JobExecutorProperties jobExecutorProperties,
      RedissonProperties redissonProperties, SystemProperties systemProperties) {
    MoreObjects.notNull(jobExecutorProperties, "job executor properties can not be null");
    MoreObjects.notNull(redissonProperties, "redisson properties can not be null");
    MoreObjects.notNull(systemProperties, "system properties can not be null");

    init(redissonProperties, systemProperties);

    CT.jobService = new RedisJobServiceImpl(CT.jobRepository, CT.jobSchedule, CT.jobLogger);
    CT.jobExecutorProperties = jobExecutorProperties;
    CT.monitor = new TimeoutMonitorImpl(CT.jobRepository, CT.publisher);
    CT.jobExecutor = CT.buildJobExecutor();
    CT.registry = new RedisRegistry(CT.redissonClient, CT.getJobExecutor());
    CT.registry.register();
  }

  /**
   * 初始化center
   *
   * @param redissonProperties redisson配置
   * @param systemProperties 系统配置
   * @param notifier 通知器
   */
  public static void initCenter(RedissonProperties redissonProperties,
      SystemProperties systemProperties, Notifier notifier) {

    MoreObjects.notNull(redissonProperties, "redisson properties can not be null");
    MoreObjects.notNull(systemProperties, "system properties can not be null");

    init(redissonProperties, systemProperties);

    CT.jobSchedule = new RedisJobScheduleImpl(CT.redissonClient, systemProperties,
        CT.jobRepository);
    CT.jobService = new RedisJobServiceImpl(CT.jobRepository, CT.jobSchedule, CT.jobLogger);
    CT.dispatcher = new RedisDispatcher(CT.redissonClient, CT.jobRepository);
    CT.listener = new EventListener(notifier);
    CT.redissonClient.getExecutorService(Container.EVENT)
        .registerWorkers(systemProperties.getParallel());
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
        jobGroup.setId(jobAnnotation.value());
        jobGroup.setJobKey(jobAnnotation.value());
        jobGroup.setJobDesc(jobAnnotation.desc());
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
        throw Exceptions.fail(String.format("job [%s] exists", jobAnnotation.value()));
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

  private static void init(RedissonProperties redissonProperties,
      SystemProperties systemProperties) {
    CT = new Container();
    CT.systemProperties = systemProperties;
    CT.redissonClient = RedisConfig.redissonClient(redissonProperties);
    CT.jobRepository = new RedisJobRepositoryImpl(CT.redissonClient);
    CT.jobLogger = new RedisJobLoggerImpl(CT.redissonClient, CT.jobRepository);
    CT.publisher = new RedisEventPublisherImpl(CT.redissonClient);
  }

  private JobExecutor buildJobExecutor() {
    JobExecutor jobExecutor = new JobExecutor();
    jobExecutor.setId(UUID.randomUUID().toString());
    jobExecutor.setName(jobExecutorProperties.getName());
    jobExecutor.setKey(jobExecutorProperties.getKey());
    String address = jobExecutorProperties.getIp();
    if (address == null || address.length() == 0) {
      address = SystemUtils.getLocalAddress().getHostAddress();
    }
    address += Strings.COLON + jobExecutorProperties.getPort();
    jobExecutor.setAddress(address);
    jobExecutor.setGroups(JOB_GROUPS);
    JOB_GROUPS.forEach(group -> group.setGroupKey(jobExecutorProperties.getKey()));
    return jobExecutor;
  }
}
