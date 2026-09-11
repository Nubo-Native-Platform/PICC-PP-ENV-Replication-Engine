package com.nnp.envrep.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * OpenAPI 3.0 / Swagger UI Configuration for PICC-PP-ENV-Replication-Engine.
 */
@Configuration
public class OpenApiConfig {

	@Value("${server.port:8082}")
	private String serverPort;

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PICC-PP-ENV-Replication-Engine REST API")
						.version("0.0.1-SNAPSHOT")
						.description("""
								REST API and Event-Driven Orchestration Engine for automated environment \
								replication and GitOps lifecycle management across the Nubo Native Platform (NNP).
								
								### Key Features:
								* **Event-Driven Provisioning**: Consumes environment replication requests via Apache ActiveMQ Artemis.
								* **Declarative GitOps Pipelines**: Generates ArgoCD root and child application manifests using FreeMarker templates.
								* **Automated GitOps Sync**: Integrates with GitLab repositories and ArgoCD REST APIs for zero-touch deployment.
								* **Dynamic Ingress Orchestration**: Coordinates service exposing and domain routing with HAProxy DataPlane API.
								* **Support Ticketing Integration**: Tracks environment creation lifecycle and support requests in Redmine.
								""")
						.contact(new Contact()
								.name("Nubo Native Platform Team")
								.email("contribution@nubons.com")
								.url("https://github.com/Nubo-Native-Platform/PICC-PP-ENV-Replication-Engine"))
						.license(new License()
								.name("Apache License 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0")))
				.servers(List.of(
						new Server().url("/").description("Default Server / Current Host"),
						new Server().url("http://localhost:" + serverPort).description("Local Development Server")
				))
				.tags(List.of(
						new Tag().name("GitOps Lifecycle").description("Endpoints and operations for managing environment replication and GitOps sync"),
						new Tag().name("Diagnostics & Health").description("Service health and operational status endpoints")
				));
	}
}
