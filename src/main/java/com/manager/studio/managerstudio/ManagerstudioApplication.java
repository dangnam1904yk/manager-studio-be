package com.manager.studio.managerstudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ManagerstudioApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManagerstudioApplication.class, args);
	}

}
