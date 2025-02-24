package com.sunny.diptish_website_crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiptishWebsiteCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiptishWebsiteCrawlerApplication.class, args);
	}

}
