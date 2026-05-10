package com.eventmate.eureka_server;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.ignoreIfMissing()
				.load();
		// Set system properties from .env file
		dotenv.entries().forEach(entry ->
			System.setProperty(entry.getKey(), entry.getValue())
		);
		System.out.println(">>> PROFILE: " + System.getProperty("SPRING_PROFILES_ACTIVE"));
		System.out.println(">>> SECURITY: " + System.getProperty("SECURITY_ENABLED"));
		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
