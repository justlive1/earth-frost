package vip.justlive.frost.core.persistence;

import java.util.List;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobExecutor;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.api.model.JobRecordStatus;
import vip.justlive.frost.api.model.JobScript;

/**
 * job持久化
 *
 * @author wubo
 */
public interface JobRepository {

  /**
   * 统计执行器个数
   *
   * @return count
   */
  int countExecutors();

  /**
   * 获取执行器列表
   *
   * @return list
   */
  List<JobExecutor> queryJobExecutors();

  /**
   * 添加job
   *
   * @param jobInfo job信息
   */
  void addJob(JobInfo jobInfo);

  /**
   * 修改job
   *
   * @param jobInfo job信息
   */
  void updateJob(JobInfo jobInfo);

  /**
   * 删除job
   *
   * @param jobId job编号
   */
  void removeJob(String jobId);

  /**
   * 统计job个数
   *
   * @return count
   */
  int countJobInfos();

  /**
   * 获取job列表
   *
   * @param from 开始
   * @param to 结束
   * @return list
   */
  List<JobInfo> queryJobInfos(int from, int to);

  /**
   * 获取所有任务
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
   * @param record 记录
   * @return 记录id
   */
  String addJobRecord(JobExecuteRecord record);

  /**
   * 获取job执行记录总数
   *
   * @param groupKey group key
   * @param jobKey job key
   * @param jobId job编号
   * @return count
   */
  int countJobRecords(String groupKey, String jobKey, String jobId);

  /**
   * 获取job执行记录列表
   *
   * @param groupKey group key
   * @param jobKey job key
   * @param jobId job编号
   * @param from 开始
   * @param to 结束
   * @return list
   */
  List<JobExecuteRecord> queryJobRecords(String groupKey, String jobKey, String jobId, int from,
      int to);

  /**
   * 根据id获取job执行记录
   *
   * @param id job编号
   * @return 执行记录
   */
  JobExecuteRecord findJobExecuteRecordById(String id);

  /**
   * 增加任务执行状态
   *
   * @param recordStatus 记录状态
   */
  void addJobRecordStatus(JobRecordStatus recordStatus);

  /**
   * 删除job执行记录
   *
   * @param jobId job编号
   */
  void removeJobRecords(String jobId);

  /**
   * 增加任务脚本
   *
   * @param script 脚本
   */
  void addJobScript(JobScript script);

  /**
   * 查询任务脚本
   *
   * @param jobId job编号
   * @return list
   */
  List<JobScript> queryJobScripts(String jobId);

  /**
   * 删除任务脚本
   *
   * @param jobId job编号
   */
  void removeJobScripts(String jobId);

  /**
   * 当前时间
   *
   * @return timestamp
   */
  long currentTime();

  /**
   * 根据类型统计
   *
   * @param type 类型
   * @return count
   */
  long countByType(String type);
}
