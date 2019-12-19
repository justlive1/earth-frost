package com.example.spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wubo
 */
@Slf4j
@Service
public class SpringService {

  public void print() {
    log.info("Enter into Spring Service");
  }
}
