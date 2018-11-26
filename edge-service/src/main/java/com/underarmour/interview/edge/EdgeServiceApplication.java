package com.underarmour.interview.edge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Edge Service Application
 *
 * @author david.moore
 */
@EnableZuulProxy
@SpringBootApplication
public class EdgeServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(EdgeServiceApplication.class, args);
  }
}
