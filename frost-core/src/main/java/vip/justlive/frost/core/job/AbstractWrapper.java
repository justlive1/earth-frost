package vip.justlive.frost.core.job;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.frost.api.model.JobExecuteRecord;
import vip.justlive.frost.api.model.JobRecordStatus;

/**
 * 抽象包装
 *
 * @author wubo
 */
@Slf4j
public abstract class AbstractWrapper implements Runnable {

  @Override
  public void run() {
    try {
      doRun();
      success();
    } catch (Exception e) {
      exception(e);
    } finally {
      finished();
    }
  }

  /**
   * run实际执行逻辑
   */
  public abstract void doRun();

  /**
   * 异常处理
   *
   * @param e 异常
   */
  public void exception(Exception e) {
    log.error("execute runnable error ", e);
  }

  /**
   * 成功处理
   */
  public void success() {
  }

  /**
   * 最终处理
   */
  public void finished() {
  }

  /**
   * job执行记录
   *
   * @param jobId job编号
   * @param loggerId 日志编号
   * @return record
   */
  protected JobExecuteRecord record(String jobId, String loggerId) {
    JobExecuteRecord record = new JobExecuteRecord();
    record.setJobId(jobId);
    record.setId(loggerId);
    return record;
  }

  /**
   * 执行记录状态
   *
   * @param loggerId 日志编号
   * @return 记录状态
   */
  protected JobRecordStatus recordStatus(String loggerId) {
    JobRecordStatus recordStatus = new JobRecordStatus();
    recordStatus.setId(UUID.randomUUID().toString());
    recordStatus.setLoggerId(loggerId);
    return recordStatus;
  }
}
