package com.eventmate.gateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

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
		SpringApplication.run(GatewayApplication.class, args);
	}

}
