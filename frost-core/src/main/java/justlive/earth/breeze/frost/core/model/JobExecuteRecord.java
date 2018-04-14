package justlive.earth.breeze.frost.core.model;

import java.util.Date;
import lombok.Data;

/**
 * job执行记录
 * 
 * @author wubo
 *
 */
@Data
public class JobExecuteRecord {

  public enum STATUS {
    /**
     * 分发成功
     */
    DISPATCH_SUCCESS(1, "分发成功"),
    /**
     * 执行成功
     */
    EXECUTE_SUCCESS(2, "执行成功"),
    /**
     * 失败
     */
    FAIL(-1, "失败");

    private int value;
    private String msg;

    STATUS(int value, String msg) {
      this.value = value;
      this.msg = msg;
    }

    public int value() {
      return value;
    }

    public String msg() {
      return msg;
    }

  }

  /**
   * 编号
   */
  private String id;

  /**
   * 任务编号
   */
  private String jobId;

  /**
   * 执行状态
   */
  private Integer status;

  /**
   * 处理消息
   */
  private String message;

  /**
   * 创建时间
   */
  private Date createAt;

}
