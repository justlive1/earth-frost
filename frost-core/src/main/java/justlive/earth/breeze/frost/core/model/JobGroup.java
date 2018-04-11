package justlive.earth.breeze.frost.core.model;

import lombok.Data;

/**
 * 任务分组
 * 
 * @author wubo
 *
 */
@Data
public class JobGroup {

  /**
   * 编号
   */
  private String id;

  /**
   * 分组key
   */
  private String groupKey;

  /**
   * 分组描述
   */
  private String groupDesc;

  /**
   * jobKey
   */
  private String jobKey;

  /**
   * job描述
   */
  private String jobDesc;

}
