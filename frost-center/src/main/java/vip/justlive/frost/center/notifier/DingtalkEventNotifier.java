package vip.justlive.frost.center.notifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.frost.core.persistence.JobRepository;
import vip.justlive.oxygen.core.ioc.BeanStore;

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

    JobInfo jobInfo =
        BeanStore.getBean(JobRepository.class).findJobInfoById(event.getData().getJobId());
    if (jobInfo == null) {
      return;
    }

    Msg msg = new Msg();
    msg.event = event;
    msg.job = jobInfo;
    EvaluationContext context = new StandardEvaluationContext(msg);

    String subjectVal = subject.getValue(context, String.class);
    String textVal = text.getValue(context, String.class);

    Map<String, Object> map = ImmutableMap.of("msgtype", "link", "link",
        ImmutableMap.of("title", subjectVal, "text", textVal, "messageUrl", linkUrl));

    String json = gson.toJson(map);

    RequestBody body = RequestBody.create(MEDIA_JSON, json);
    Request request = new Request.Builder().url(url).post(body).build();
    try {
      okhttp3.Response resp = client.newCall(request).execute();
      String respBody = resp.body().string();
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
