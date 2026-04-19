package com.eventmate.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
public class TenantApplication {

	private static final Logger log = LoggerFactory.getLogger(TenantApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TenantApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		log.info("Tenant Service Application Started Successfully!");
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
