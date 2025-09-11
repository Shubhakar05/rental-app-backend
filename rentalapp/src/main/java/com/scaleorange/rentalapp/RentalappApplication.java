package com.scaleorange.rentalapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentalappApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalappApplication.class, args);
	}

}
