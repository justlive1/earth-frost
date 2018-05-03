package justlive.earth.breeze.frost.core.notify;

import justlive.earth.breeze.frost.api.model.JobInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
  private JobInfo job;

  /**
   * 类型
   */
  private String type;

  /**
   * 消息
   */
  private String message;

  /**
   * 时间戳
   */
  private long timestamp;


}
