package com.example.urlcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlCacheApplication {

	public static void main(String[] args) {
		// Runs the passed in args against CacheRunner (@Bean)
		SpringApplication.run(UrlCacheApplication.class, args);
	}

}
