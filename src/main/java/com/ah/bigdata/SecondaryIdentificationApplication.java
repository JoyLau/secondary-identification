package com.ah.bigdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SecondaryIdentificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecondaryIdentificationApplication.class, args);
	}
}
