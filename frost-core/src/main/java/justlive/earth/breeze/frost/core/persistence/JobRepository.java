package justlive.earth.breeze.frost.core.persistence;

import java.util.List;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobInfo;

/**
 * job持久化
 * 
 * @author wubo
 *
 */
public interface JobRepository {

  /**
   * 统计执行器个数
   * 
   * @return
   */
  int countExecutors();

  /**
   * 获取执行器列表
   * 
   * @return
   */
  List<JobExecutor> queryJobExecutors();

  /**
   * 添加job
   * 
   * @param jobInfo
   */
  void addJob(JobInfo jobInfo);

  /**
   * 修改job
   * 
   * @param jobInfo
   */
  void updateJob(JobInfo jobInfo);

  /**
   * 删除job
   * 
   * @param jobId
   */
  void removeJob(String jobId);

  /**
   * 统计job个数
   * 
   * @return
   */
  int countJobInfos();

  /**
   * 获取job列表
   * 
   * @return
   */
  List<JobInfo> queryJobInfos();

  /**
   * 根据id获取job
   * 
   * @param id
   * @return
   */
  JobInfo findJobInfoById(String id);

  /**
   * 添加job执行记录
   * 
   * @param record
   * @return
   */
  String addJobRecord(JobExecuteRecord record);

  /**
   * 获取job执行记录列表
   * 
   * @return
   */
  List<JobExecuteRecord> queryJobRecords(String jobId, int from, int to);
}
