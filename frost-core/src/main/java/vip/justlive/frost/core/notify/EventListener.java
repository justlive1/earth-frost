package vip.justlive.frost.core.notify;

import lombok.Data;

/**
 * 事件监听
 *
 * @author wubo
 */
@Data
public class EventListener {

  private final Notifier notifier;

  /**
   * 处理事件
   *
   * @param event 事件
   */
  public void onEvent(Event event) {
    if (notifier != null) {
      notifier.notify(event);
    }
  }
}
