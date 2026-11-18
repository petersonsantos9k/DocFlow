package com.docflow;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

	static void main(String[] args) {
		var builder = new SpringApplicationBuilder(Application.class);
		builder.run(args);
	}
}
