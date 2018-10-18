package vip.justlive.frost.center.notifier;

import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.notify.Notifier;

/**
 * 组合通知
 * 
 * @author wubo
 *
 */
public class CompositeNotifier extends AbstractEventNotifier {

  private final Iterable<Notifier> delegates;

  public CompositeNotifier(Iterable<Notifier> delegates) {
    this.delegates = delegates;
  }

  @Override
  protected void doNotify(Event event) {
    for (Notifier notifier : delegates) {
      notifier.notify(event);
    }
  }
}
