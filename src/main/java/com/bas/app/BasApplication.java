package com.bas.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@ComponentScan({"com.bas.jdbc"})
public class BasApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasApplication.class, args);
	}

}
