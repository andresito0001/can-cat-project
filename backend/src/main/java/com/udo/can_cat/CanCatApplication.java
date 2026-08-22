package com.udo.can_cat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CanCatApplication {

	public static void main(String[] args) {
		SpringApplication.run(CanCatApplication.class, args);
	}

}
