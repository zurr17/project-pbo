package com.sarpraskost.app;

import java.util.TimeZone;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication {

	@PostConstruct
	public void initTimezone() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));
	}

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));
		SpringApplication.run(AppApplication.class, args);
	}

}
