package com.docflow;

import com.docflow.extraction.domain.model.*;
import com.docflow.extraction.orchestrator.ExtractionOrchestrator;
import com.docflow.ingestion.config.IngestionProperties;
import com.docflow.ingestion.orchestrator.IngestionService;
import com.docflow.ingestion.orchestrator.IngestionSession;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@SpringBootApplication
@EnableConfigurationProperties(IngestionProperties.class)
public class Application {


	static void main(String[] args) throws IOException {


		var builder = new SpringApplicationBuilder(Application.class);
		builder.run(args);

		ApplicationContext applicationContext = builder.context();
		IngestionService bean = applicationContext.getBean(IngestionService.class);

		try (IngestionSession session = bean.ingest("https://github.com/petersonsantos9k/picpay-desafio-backend")) {
			IO.println(session);
			//ExtractionOrchestrator bean1 = applicationContext.getBean(ExtractionOrchestrator.class);




			/*
			for(ControllerInfo controller : projectModel.controllers()){
				System.out.println("Controller: " + controller.name());
				System.out.println("BasePath:   " + controller.basePath());
				System.out.println("Endpoints:  " + controller.endpoints().size());

				for (EndpointInfo endpoint : controller.endpoints()) {
					System.out.println();
					System.out.println("  " + endpoint + " " + endpoint.path());
					System.out.println("    método:  " + endpoint.methodName());
					System.out.println("    retorno: " + endpoint.returnType());
					System.out.println("    status:  " + endpoint.status());
					System.out.println("    dtos:    " + endpoint.dtos());

					for (ParameterInfo param : endpoint.parameters()) {
						System.out.println("    param:   " + param.name()
								+ " (" + param.location() + ", " + param.type()
								+ ", required=" + param.required() + ")");
					}
				}
			}
			*/
		}



	}

}
