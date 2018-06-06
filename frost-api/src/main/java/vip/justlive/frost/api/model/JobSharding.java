package vip.justlive.frost.api.model;

import lombok.Data;

/**
 * 分片
 * 
 * @author wubo
 *
 */
@Data
public class JobSharding {

  /**
   * index
   */
  private int index;

  /**
   * 总数
   */
  private int total;

}
