package vip.justlive.frost.client;

import org.junit.Assert;
import org.junit.Before;
import vip.justlive.frost.api.facade.JobApiFacade;
import vip.justlive.frost.api.model.JobGroup;
import vip.justlive.frost.api.model.JobInfo;
import vip.justlive.oxygen.core.config.ConfigFactory;

/**
 * 测试
 *
 * @author wubo
 */
public class FacadeBuilderTest {

  JobApiFacade jobApiFacade;

  @Before
  public void before() {
    ConfigFactory.loadProperties("classpath:frost.properties");
    ClientProperties clientProps = ConfigFactory.load(ClientProperties.class);
    jobApiFacade = new JobApiFacadeImpl(clientProps);
  }

  @org.junit.Test
  public void test() {
//    testJob();
  }

  public void testJob() {

    JobInfo jobInfo = new JobInfo();
    jobInfo.setName("测试添加");
    jobInfo.setType(JobInfo.TYPE.BEAN.name());
    JobGroup group = new JobGroup();
    group.setGroupKey("executor-demo");
    group.setJobKey("printTimeJob");
    jobInfo.setGroup(group);
    jobInfo.setCron("0 * * * * ?");
    jobInfo.setFailStrategy(JobInfo.STRATEGY.NOTIFY.name());
    jobInfo.setMode(JobInfo.MODE.CRON.name());
    String id = jobApiFacade.addJob(jobInfo);

    Assert.assertNotNull(id);

    jobApiFacade.triggerJob(id);
    jobApiFacade.resumeJob(id);
    jobApiFacade.pauseJob(id);
    jobInfo.setCron("1 * * * * ?");
    jobInfo.setId(id);
    jobApiFacade.updateJob(jobInfo);
    jobApiFacade.removeJob(id);

  }


}
