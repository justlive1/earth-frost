package vip.justlive.frost.core.notify;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.job.AbstractWrapper;

/**
 * 事件执行包装
 *
 * @author wubo
 */
@NoArgsConstructor
@AllArgsConstructor
public class EventExecuteWrapper extends AbstractWrapper {

  private Event event;

  @Override
  public void doRun() {
    EventListener listener = Container.get().getListener();
    if (listener != null) {
      listener.onEvent(event);
    }
  }
}
