package vip.justlive.frost.center.notifier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * 邮件事件通知器
 *
 * @author wubo
 */
@Setter
@RequiredArgsConstructor
public class MailEventNotifier extends AbstractEventNotifier {

  private static final List<String> SUPPORT_EVENTS = Arrays
      .asList(Event.TYPE.DISPATCH_FAIL.name(), Event.TYPE.EXECUTE_FAIL.name(),
          Event.TYPE.TIMEOUT_MONITOR.name());

  private final MailSender sender;

  /**
   * 邮件接收者
   */
  private String[] to;

  /**
   * 邮件抄送
   */
  private String[] cc;

  /**
   * 邮件发送者
   */
  private String from;

  @Override
  protected boolean shouldNotify(Event event) {
    return SUPPORT_EVENTS.contains(event.getType());
  }

  @Override
  protected void doNotify(Event event) {

    JobInfo jobInfo = Container.get().getJobRepository()
        .findJobInfoById(event.getData().getJobId());
    if (jobInfo == null) {
      return;
    }

    Map<String, Object> attrs = MoreObjects.mapOf("event", event, "job", jobInfo);

    String subjectVal = ENGINE.render(DEFAULT_SUBJECT, attrs);
    String textVal = ENGINE.render(DEFAULT_TEXT, attrs);

    SimpleMailMessage message = new SimpleMailMessage();

    String[] mails = jobInfo.getNotifyMails();
    String[] dest;
    if (mails != null) {
      dest = Arrays.copyOf(mails, mails.length + to.length);
      System.arraycopy(to, 0, dest, mails.length, to.length);
    } else {
      dest = to;
    }
    message.setTo(dest);
    message.setFrom(from);
    message.setSubject(subjectVal);
    message.setText(textVal);
    message.setCc(cc);

    sender.send(message);
  }
}

