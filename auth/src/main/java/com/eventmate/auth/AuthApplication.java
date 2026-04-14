package com.eventmate.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthApplication {

	private static final Logger log = LoggerFactory.getLogger(AuthApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
		log.info("EventMate Auth Application started successfully");
	}

}
