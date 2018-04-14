package justlive.earth.breeze.frost.core.service;

import java.util.List;
import justlive.earth.breeze.frost.core.model.JobExecuteRecord;
import justlive.earth.breeze.frost.core.model.JobExecutor;
import justlive.earth.breeze.frost.core.model.JobInfo;

/**
 * 调度中心服务接口
 * 
 * @author wubo
 *
 */
public interface JobService {

  /**
   * 获取激活的执行器列表
   * 
   * @return
   */
  List<JobExecutor> queryExecutors();

  /**
   * 添加Job
   * 
   * @param jobInfo
   * @return
   */
  String addJob(JobInfo jobInfo);

  /**
   * 修改job
   * 
   * @param jobInfo
   */
  void updateJob(JobInfo jobInfo);

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
