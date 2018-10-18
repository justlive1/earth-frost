package vip.justlive.frost.executor.example;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.frost.core.job.Job;
import vip.justlive.frost.core.job.JobContext;
import vip.justlive.oxygen.core.ioc.Bean;

/**
 * 打印时间job，例子
 * 
 * @author wubo
 *
 */
@Slf4j
@Job(value = "printTimeJob", desc = "打印时间job例子")
@Bean
public class PrintTimeJob extends BaseJob {

  @Override
  public void execute(JobContext ctx) {
    log.info("current time is {}", LocalDateTime.now());
  }

}
