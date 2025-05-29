package com.example.CollabAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Collections;


@SpringBootApplication
public class CollabAuthApplication {
	private static final Logger logger = LoggerFactory.getLogger(CollabAuthApplication.class);
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(CollabAuthApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", 4001));
		app.run(args);
		logger.info("Application running successfully on port 4001");
	}
}
