package com.dylan.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MongoTest1Application {

	public static void main(String[] args) {
		SpringApplication.run(MongoTest1Application.class, args);
	}

	@RequestMapping("/test1")
	public String test1() {
		return "hello world!";
	}
}
