package org.openingo.demo.kongoauth2;

import org.openingo.spring.annotation.EnableExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableExtension
public class KongOauth2Application {

	public static void main(String[] args) {
		SpringApplication.run(KongOauth2Application.class, args);
	}

}
