package justlive.earth.breeze.frost.core.model;

import lombok.Data;

/**
 * job信息
 * 
 * @author wubo
 *
 */
@Data
public class JobInfo {

  /**
   * 编号
   */
  private String id;

  /**
   * 名称
   */
  private String name;

  /**
   * 分组编号
   */
  private JobGroup group;

  /**
   * 定时表达式
   */
  private String triggle;

  /**
   * 任务状态
   */
  private Integer status;

}
