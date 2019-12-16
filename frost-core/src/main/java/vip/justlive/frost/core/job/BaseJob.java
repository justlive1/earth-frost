package vip.justlive.frost.core.job;

import vip.justlive.frost.core.config.Container;

/**
 * job
 *
 * @author wubo
 */
public abstract class BaseJob {

  public BaseJob() {
    Container.addJob(this);
  }

  /**
   * 初始化
   */
  public void init() {
  }

  /**
   * 销毁
   */
  public void destroy() {
  }

  /**
   * 执行
   *
   * @param ctx 上下文
   */
  public abstract void execute(JobContext ctx);

  /**
   * 异常处理，默认返回true。
   * <br>
   * 返回true时使用全局异常处理逻辑
   * <br>
   * 当需要对部分job特殊处理异常时，在实现类重写该方法并返回false
   *
   * @return true为全局处理
   */
  public boolean exception() {
    return true;
  }
}
