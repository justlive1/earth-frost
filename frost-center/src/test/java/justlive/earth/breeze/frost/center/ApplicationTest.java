package justlive.earth.breeze.frost.center;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import justlive.earth.breeze.frost.core.dispacher.Dispatcher;
import justlive.earth.breeze.frost.core.model.JobGroup;
import justlive.earth.breeze.frost.core.model.JobInfo;

/**
 * 测试分发
 * 
 * @author wubo
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

  @Autowired
  Dispatcher dispatcher;

  @Test
  public void test() {

    JobInfo jobInfo = new JobInfo();
    JobGroup group = new JobGroup();
    group.setGroupKey("executor-demo");
    group.setJobKey("printTimeJob");
    jobInfo.setGroup(group);

    dispatcher.dispatch(jobInfo);

  }

}
