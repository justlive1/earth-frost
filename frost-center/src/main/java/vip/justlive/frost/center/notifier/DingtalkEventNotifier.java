package vip.justlive.frost.center.notifier;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.oxygen.core.net.http.HttpRequest;
import vip.justlive.oxygen.core.net.http.HttpResponse;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * 钉钉事件通知
 *
 * @author wubo
 */
public class DingtalkEventNotifier extends AbstractEventNotifier {

  private static final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");
  private static final String DINGTALK_URL = "https://oapi.dingtalk.com/robot/send?access_token=%s";
  private static final List<String> SUPPORT_EVENTS = Arrays.asList(Event.TYPE.DISPATCH_FAIL.name(),
      Event.TYPE.EXECUTE_FAIL.name(), Event.TYPE.TIMEOUT_MONITOR.name());

  /**
   * 发送文本
   */
  private Expression text;

  /**
   * 发送主题
   */
  private Expression subject;

  /**
   * 访问令牌
   */
  private String accessToken;

  /**
   * 链接地址
   */
  private String linkUrl = "localhost:20000/center";

  private String url;

  private OkHttpClient client = new OkHttpClient.Builder().build();

  private Gson gson = new Gson();

  public DingtalkEventNotifier() {
    this.subject = PARSER.parseExpression(DEFAULT_SUBJECT, ParserContext.TEMPLATE_EXPRESSION);
    this.text = PARSER.parseExpression(DEFAULT_TEXT, ParserContext.TEMPLATE_EXPRESSION);
  }

  @PostConstruct
  void init() {
    this.url = String.format(DINGTALK_URL, this.accessToken);
  }

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

    Msg msg = new Msg();
    msg.event = event;
    msg.job = jobInfo;
    EvaluationContext context = new StandardEvaluationContext(msg);

    String subjectVal = subject.getValue(context, String.class);
    String textVal = text.getValue(context, String.class);

    Map<String, Object> map = MoreObjects.mapOf("msgtype", "link", "link",
        MoreObjects.mapOf("title", subjectVal, "text", textVal, "messageUrl", linkUrl));

    String json = gson.toJson(map);

    try (HttpResponse response = HttpRequest.post(url).jsonBody(json).execute()) {
      String respBody = response.bodyAsString();
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("dingtalk resp: {}", respBody);
      }
    } catch (IOException e) {
      getLogger().error("dingtalk error: {}", e);
    }
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setSubject(String subject) {
    this.subject = PARSER.parseExpression(subject, ParserContext.TEMPLATE_EXPRESSION);
  }

  public String getSubject() {
    return subject.getExpressionString();
  }

  public void setText(String text) {
    this.text = PARSER.parseExpression(text, ParserContext.TEMPLATE_EXPRESSION);
  }

  public String getText() {
    return text.getExpressionString();
  }

  @Data
  public static class Msg {

    private Event event;
    private JobInfo job;
  }
}
