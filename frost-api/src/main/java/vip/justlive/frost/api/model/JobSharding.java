package vip.justlive.frost.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片
 * 
 * @author wubo
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
