package com.example.spring.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.spring.service.SpringService;
import vip.justlive.frost.core.job.BaseJob;
import vip.justlive.frost.core.job.Job;
import vip.justlive.frost.core.job.JobContext;

/**
 * 使用Spring的例子
 * 
 * @author wubo
 *
 */
@Component
@Job(value = "springJob", desc = "使用Spring依赖的Job")
public class SpringJob extends BaseJob {

  @Autowired
  private SpringService service;

  @Override
  public void execute(JobContext ctx) {
    service.print();
  }

}
