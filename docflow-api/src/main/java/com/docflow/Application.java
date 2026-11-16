package com.docflow;

import com.docflow.ingestion.config.IngestionProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties(IngestionProperties.class)
public class Application {

	public static void main(String[] args) {
		var builder = new SpringApplicationBuilder(Application.class);
		builder.run(args);

		ApplicationContext applicationContext = builder.context();
		applicationContext.getBean(DemoRunner.class).run();
	}
}
