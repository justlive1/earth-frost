package vip.justlive.frost.core.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 事件通知抽象类
 * 
 * @author wubo
 *
 */
public abstract class AbstractEventNotifier implements Notifier {

  protected static final String DEFAULT_SUBJECT = "#{job.name} (#{job.id}) throws an exception";
  protected static final String DEFAULT_TEXT = "#{job.name} (#{job.id}) \n #{event.message}";
  protected static final SpelExpressionParser PARSER = new SpelExpressionParser();

  /**
   * 是否启用
   */
  private boolean enabled = true;

  @Override
  public void notify(Event event) {
    if (enabled && shouldNotify(event)) {
      try {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("通知事件:{}", event);
        }
        doNotify(event);
      } catch (Exception ex) {
        getLogger().error("事件通知失败 {} ", event, ex);
      }
    }
  }


  protected boolean shouldNotify(Event event) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("通知事件:{}", event);
    }
    return true;
  }

  /**
   * 处理通知逻辑
   * 
   * @param event
   */
  protected abstract void doNotify(Event event);

  protected Logger getLogger() {
    return LoggerFactory.getLogger(this.getClass());
  }
}
