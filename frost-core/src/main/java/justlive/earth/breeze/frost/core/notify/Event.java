package justlive.earth.breeze.frost.core.notify;

import justlive.earth.breeze.frost.api.model.JobInfo;
import lombok.Data;

@Data
public class Event {

  public enum TYPE {
    /**
     * 调度失败
     */
    DISPATCH_FAIL,
    /**
     * 执行失败
     */
    EXECUTE_FAIL
  }

  /**
   * job
   */
  private final JobInfo job;

  /**
   * 类型
   */
  private final String type;

  /**
   * 消息
   */
  private final String message;

  /**
   * 时间戳
   */
  private final long timestamp;


}
