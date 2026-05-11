package com.derabbit.seolstudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeolstudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeolstudyApplication.class, args);
	}

}
