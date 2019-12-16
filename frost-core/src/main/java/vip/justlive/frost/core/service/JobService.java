package vip.justlive.frost.core.service;

import java.util.List;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobScript;
import vip.justlive.frost.api.model.JobStatistic;
import vip.justlive.frost.api.model.Page;

/**
 * 调度中心服务接口
 *
 * @author wubo
 */
public interface JobService {

  /**
   * 统计执行器个数
   *
   * @return connt
   */
  int countExecutors();

  /**
   * 获取激活的执行器列表
   *
   * @return list
   */
  List<JobExecutor> queryExecutors();

  /**
   * 添加Job
   *
   * @param jobInfo job信息
   * @return jobId
   */
  String addJob(JobInfo jobInfo);

  /**
   * 修改job
   *
   * @param jobInfo job信息
   */
  void updateJob(JobInfo jobInfo);

  /**
   * 暂停job
   *
   * @param jobId job编号
   */
  void pauseJob(String jobId);

  /**
   * 恢复job
   *
   * @param jobId job编号
   */
  void resumeJob(String jobId);

  /**
   * 删除job
   *
   * @param jobId job编号
   */
  void removeJob(String jobId);

  /**
   * 触发job
   *
   * @param jobId job编号
   */
  void triggerJob(String jobId);

  /**
   * 统计job个数
   *
   * @return count
   */
  int countJobInfos();

  /**
   * 获取job列表
   *
   * @param pageIndex 第几页
   * @param pageSize 每页条数
   * @return page
   */
  Page<JobInfo> queryJobInfos(int pageIndex, int pageSize);

  /**
   * 获取所有job
   *
   * @return list
   */
  List<JobInfo> queryAllJobs();

  /**
   * 根据id获取job
   *
   * @param id job编号
   * @return jobInfo
   */
  JobInfo findJobInfoById(String id);

  /**
   * 添加job执行记录
   *
   * @param record 执行记录
   * @return 记录id
   */
  String addJobRecord(JobExecuteRecord record);

  /**
   * 获取job执行记录列表
   *
   * @param groupKey group key
   * @param jobKey job key
   * @param jobId job编号
   * @param pageIndex 第几页
   * @param pageSize 每页条数
   * @return page
   */
  Page<JobExecuteRecord> queryJobRecords(String groupKey, String jobKey, String jobId,
      int pageIndex, int pageSize);

  /**
   * 增加任务脚本
   *
   * @param script 脚本
   */
  void addJobScript(JobScript script);

  /**
   * 查询脚本列表
   *
   * @param jobId job编号
   * @return list
   */
  List<JobScript> queryJobScripts(String jobId);

  /**
   * 获取统计
   *
   * @param begin 开始
   * @param end 结束
   * @return 统计
   */
  JobStatistic queryJobStatistic(String begin, String end);

  /**
   * 删除job日志
   *
   * @param jobId job编号
   */
  void removeJobRecords(String jobId);
}
