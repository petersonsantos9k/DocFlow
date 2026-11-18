package com.docflow;

import com.docflow.ingestion.config.IngestionProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IngestionProperties.class)
public class Application {

	static void main(String[] args) {
		var builder = new SpringApplicationBuilder(Application.class);
		builder.run(args);
	}
}
