package com.echolife.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EchoLifeIdentityConsentApplication {
  public static void main(String[] args) { SpringApplication.run(EchoLifeIdentityConsentApplication.class, args); }
}
