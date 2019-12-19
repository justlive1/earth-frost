package vip.justlive.frost.center.notifier;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Setter;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.frost.core.config.Container;
import vip.justlive.frost.core.notify.Event;
import vip.justlive.oxygen.core.crypto.MacEncoder;
import vip.justlive.oxygen.core.net.http.HttpRequest;
import vip.justlive.oxygen.core.net.http.HttpResponse;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * 钉钉事件通知
 *
 * @author wubo
 */
@Setter
public class DingtalkEventNotifier extends AbstractEventNotifier {

  private static final List<String> SUPPORT_EVENTS = Arrays.asList(Event.TYPE.DISPATCH_FAIL.name(),
      Event.TYPE.EXECUTE_FAIL.name(), Event.TYPE.TIMEOUT_MONITOR.name());

  /**
   * 访问令牌
   */
  private String accessToken;
  private String algorithm = "HmacSHA256";
  private String secret;
  private String url = "https://oapi.dingtalk.com/robot/send";

  /**
   * 链接地址
   */
  private String linkUrl = "localhost:20000/center";

  private Gson gson = new Gson();
  private MacEncoder encoder;

  @PostConstruct
  private void init() {
    encoder = new MacEncoder(algorithm, secret).useBase64(true);
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

    Map<String, Object> attrs = MoreObjects.mapOf("event", event, "job", jobInfo);

    String subjectVal = ENGINE.render(DEFAULT_SUBJECT, attrs);
    String textVal = ENGINE.render(DEFAULT_TEXT, attrs);

    long timestamp = System.currentTimeMillis();
    String sign = encoder.encode(timestamp + "\n" + secret);

    Map<String, Object> query = new HashMap<>(4);
    query.put("access_token", accessToken);
    query.put("timestamp", timestamp);
    query.put("sign", sign);

    Map<String, Object> map = MoreObjects.mapOf("msgtype", "link", "link",
        MoreObjects.mapOf("title", subjectVal, "text", textVal, "messageUrl", linkUrl));

    String json = gson.toJson(map);

    try (HttpResponse response = HttpRequest.post(url).queryParam(query).jsonBody(json).execute()) {
      String respBody = response.bodyAsString();
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("dingtalk resp: {}", respBody);
      }
    } catch (IOException e) {
      getLogger().error("dingtalk error: {}", e);
    }
  }

}
