package justlive.earth.breeze.frost.core.job;

/**
 * job
 * 
 * @author wubo
 *
 */
public interface IJob {

  /**
   * 初始化
   */
  default void init() {}

  /**
   * 销毁
   */
  default void destroy() {}

  /**
   * 执行
   */
  void execute(JobContext ctx);
}
