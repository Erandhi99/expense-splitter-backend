package com.example.expense_splitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.expense_splitter.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ExpenseSplitterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenseSplitterApplication.class, args);
	}

}
