package justlive.earth.breeze.frost.core.notify;

import java.util.Arrays;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import com.google.common.base.Objects;
import justlive.earth.breeze.frost.api.model.JobInfo;

public class MailEventNotifier extends AbstractEventNotifier {

  private static final String DEFAULT_SUBJECT = "#{job.name} (#{job.id}) throws an exception";
  private static final String DEFAULT_TEXT = "#{job.name} (#{job.id}) \n #{message}";

  private final SpelExpressionParser parser = new SpelExpressionParser();
  private final MailSender sender;

  /**
   * 邮件接收者
   */
  private String[] to = {"root@localhost"};

  /**
   * 邮件抄送
   */
  private String[] cc;

  /**
   * 邮件发送者
   */
  private String from = null;

  /**
   * 邮件文本
   */
  private Expression text;

  /**
   * 邮件主题
   */
  private Expression subject;

  public MailEventNotifier(MailSender sender) {
    this.sender = sender;
    this.subject = parser.parseExpression(DEFAULT_SUBJECT, ParserContext.TEMPLATE_EXPRESSION);
    this.text = parser.parseExpression(DEFAULT_TEXT, ParserContext.TEMPLATE_EXPRESSION);
  }

  @Override
  protected void doNotify(Event event) {

    EvaluationContext context = new StandardEvaluationContext(event);

    SimpleMailMessage message = new SimpleMailMessage();
    String[] mails = event.getJob().getNotifyMails();
    String[] dest;
    if (mails != null) {
      dest = Arrays.copyOf(mails, mails.length + to.length);
      System.arraycopy(to, 0, dest, mails.length, to.length);
    } else {
      dest = to;
    }
    message.setTo(dest);
    message.setFrom(from);
    message.setSubject(subject.getValue(context, String.class));
    message.setText(text.getValue(context, String.class));
    message.setCc(cc);

    sender.send(message);
  }

  @Override
  protected boolean shouldNotify(Event event) {
    return event.getJob().getFailStrategy() == null
        || Objects.equal(event.getJob().getFailStrategy(), JobInfo.STRATEGY.NOTIFY.name());
  }

  public void setTo(String[] to) {
    this.to = Arrays.copyOf(to, to.length);
  }

  public String[] getTo() {
    return Arrays.copyOf(to, to.length);
  }

  public void setCc(String[] cc) {
    this.cc = Arrays.copyOf(cc, cc.length);
  }

  public String[] getCc() {
    return Arrays.copyOf(cc, cc.length);
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getFrom() {
    return from;
  }

  public void setSubject(String subject) {
    this.subject = parser.parseExpression(subject, ParserContext.TEMPLATE_EXPRESSION);
  }

  public String getSubject() {
    return subject.getExpressionString();
  }

  public void setText(String text) {
    this.text = parser.parseExpression(text, ParserContext.TEMPLATE_EXPRESSION);
  }

  public String getText() {
    return text.getExpressionString();
  }

}
