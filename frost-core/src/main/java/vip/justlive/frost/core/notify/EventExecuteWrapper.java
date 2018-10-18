package vip.justlive.frost.core.notify;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import vip.justlive.frost.core.job.AbstractWrapper;
import vip.justlive.oxygen.core.ioc.BeanStore;

/**
 * 事件执行包装
 * 
 * @author wubo
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class EventExecuteWrapper extends AbstractWrapper {

  private Event event;

  @Override
  public void doRun() {
    EventListener listener = BeanStore.getBean(EventListener.class);
    listener.onEvent(event);
  }
}
