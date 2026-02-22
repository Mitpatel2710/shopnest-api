package com.shopnest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing          // enables @CreatedDate, @LastModifiedDate in EC-015A
public class ShopnestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopnestApiApplication.class, args);
	}
}