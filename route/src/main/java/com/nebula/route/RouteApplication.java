package com.nebula.route;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.nebula.route.mapper")
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableDubbo
public class RouteApplication {
	public static void main(String[] args) {
		SpringApplication.run(RouteApplication.class, args);
	}
}