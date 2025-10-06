package com.electroshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ElectroshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElectroshopApplication.class, args);
	}
}


