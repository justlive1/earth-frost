package vip.justlive.frost.client;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import vip.justlive.frost.api.facade.JobApiFacade;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.net.http.HttpRequest;
import vip.justlive.oxygen.core.net.http.HttpResponse;
import vip.justlive.oxygen.core.util.MoreObjects;
import vip.justlive.oxygen.core.util.Resp;
import vip.justlive.oxygen.core.util.Strings;

/**
 * job api facade 实现类
 *
 * @author wubo
 */
public class JobApiFacadeImpl implements JobApiFacade {

  private Gson gson = new Gson();
  private ClientProperties clientProps;
  private String auth;

  public JobApiFacadeImpl(ClientProperties clientProps) {
    this.clientProps = clientProps;
    this.auth = "Basic " + Base64.getEncoder().encodeToString(
        (clientProps.getUsername() + Strings.COLON + clientProps.getPassword())
            .getBytes(StandardCharsets.UTF_8));
  }

  String handle(HttpRequest request) {
    try (HttpResponse response = request.addHeader("Authorization", auth).execute()) {
      Resp resp = gson.fromJson(response.bodyAsString(), Resp.class);
      if (!resp.isSuccess()) {
        throw Exceptions.fail(resp.getCode(), resp.getMessage());
      }
      Object data = resp.getData();
      if (data != null) {
        return data.toString();
      }
      return null;
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public String addJob(JobInfo jobInfo) {
    return handle(
        HttpRequest.post(clientProps.getBaseUrl() + "/addJob").jsonBody(gson.toJson(jobInfo)));
  }

  @Override
  public void updateJob(JobInfo jobInfo) {
    handle(
        HttpRequest.post(clientProps.getBaseUrl() + "/updateJob").jsonBody(gson.toJson(jobInfo)));
  }

  @Override
  public void pauseJob(String jobId) {
    handle(HttpRequest.post(clientProps.getBaseUrl() + "/pauseJob")
        .formBody(MoreObjects.mapOf("id", jobId)));
  }

  @Override
  public void resumeJob(String jobId) {
    handle(HttpRequest.post(clientProps.getBaseUrl() + "/resumeJob")
        .formBody(MoreObjects.mapOf("id", jobId)));
  }

  @Override
  public void removeJob(String jobId) {
    handle(HttpRequest.post(clientProps.getBaseUrl() + "/removeJob")
        .formBody(MoreObjects.mapOf("id", jobId)));
  }

  @Override
  public void triggerJob(String jobId) {
    handle(HttpRequest.post(clientProps.getBaseUrl() + "/triggerJob")
        .formBody(MoreObjects.mapOf("id", jobId)));
  }

}
