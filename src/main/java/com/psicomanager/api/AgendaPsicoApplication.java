package com.psicomanager.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgendaPsicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendaPsicoApplication.class, args);
	}

}
