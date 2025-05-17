package com.vb.fundraiser;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@OpenAPIDefinition(
		info = @Info(
				title = "Fundraiser API",
				version = "1.0",
				description = "REST API for managing fundraising events and collection boxes"
		)
)
@SpringBootApplication
@EnableCaching
public class FundraiserApplication {
	public static void main(String[] args) {
		SpringApplication.run(FundraiserApplication.class, args);
	}

}
