package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.example.demo.model.PageCriteria;

@SpringBootApplication
@ComponentScan("com.example.demo")
public class MybatisPageableApplication {

	public static void main(String[] args) {
		SpringApplication.run(MybatisPageableApplication.class, args);
	}

}
