package com.java_refresh.day4_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Day4DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(Day4DemoApplication.class, args);
	}

}
