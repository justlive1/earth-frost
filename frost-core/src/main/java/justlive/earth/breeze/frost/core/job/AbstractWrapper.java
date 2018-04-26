package justlive.earth.breeze.frost.core.job;

import java.util.UUID;
import justlive.earth.breeze.frost.api.model.JobExecuteRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象包装
 * 
 * @author wubo
 *
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
      finshed();
    }
  }

  /**
   * run实际执行逻辑
   */
  public abstract void doRun();

  /**
   * 异常处理
   * 
   * @param e
   */
  public void exception(Exception e) {
    log.error("execute runnable error ", e);
  }

  /**
   * 成功处理
   */
  public void success() {}

  /**
   * 最终处理
   */
  public void finshed() {}

  /**
   * job执行记录
   * 
   * @param jobId
   * @return
   */
  protected JobExecuteRecord record(String jobId) {
    JobExecuteRecord record = new JobExecuteRecord();
    record.setJobId(jobId);
    record.setId(UUID.randomUUID().toString());
    return record;
  }
}
