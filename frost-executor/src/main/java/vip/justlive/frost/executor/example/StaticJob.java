package vip.justlive.frost.executor.example;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.frost.core.job.Job;
import vip.justlive.frost.core.job.JobContext;
import vip.justlive.oxygen.ioc.annotation.Bean;

/**
 * 统计job
 *
 * @author wubo
 */
@Slf4j
@Job(value = "staticJob", desc = "统计job")
@Bean
public class StaticJob extends BaseJob {

  private Random random = new Random();

  @Override
  public void execute(JobContext ctx) {
    log.info("there are {} jobs", random.nextInt(10));
  }

}
