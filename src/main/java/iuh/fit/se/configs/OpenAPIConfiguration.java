package iuh.fit.se.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfiguration {
    final String securitySchemeName = "bearerAuth";
	@Bean
	public OpenAPI defineOpenApi() {
		Server server = new Server();
		server.setUrl("/");
		server.setDescription("REST API Documentation");

		Info information = new Info()
				.title("REST API Documentation")
				.version("1.0")
				.description("This API exposes endpoints to manage employees.");

		return new OpenAPI()
                .info(information)
                .servers(List.of(server))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                );
    }
}