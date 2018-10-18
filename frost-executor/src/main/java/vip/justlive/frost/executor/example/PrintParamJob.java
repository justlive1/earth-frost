package vip.justlive.frost.executor.example;

import lombok.extern.slf4j.Slf4j;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.frost.core.job.Job;
import vip.justlive.frost.core.job.JobContext;
import vip.justlive.oxygen.core.ioc.Bean;

/**
 * 打印执行参数job例子
 * 
 * @author wubo
 *
 */
@Slf4j
@Job(value = "printParamJob", desc = "打印执行参数job例子")
@Bean
public class PrintParamJob extends BaseJob {

  @Override
  public void execute(JobContext ctx) {
    log.info("执行参数: {}", ctx.getParam());
  }
}
