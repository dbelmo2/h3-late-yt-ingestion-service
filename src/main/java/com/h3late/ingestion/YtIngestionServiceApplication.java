package com.h3late.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YtIngestionServiceApplication {
	static void main(String[] args) {
		SpringApplication.run(YtIngestionServiceApplication.class, args);
	}
}
