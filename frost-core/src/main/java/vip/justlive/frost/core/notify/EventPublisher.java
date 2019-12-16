package vip.justlive.frost.core.notify;

/**
 * 事件发布接口
 *
 * @author wubo
 */
public interface EventPublisher {

  /**
   * 发布
   *
   * @param event 事件
   */
  void publish(Event event);
}
