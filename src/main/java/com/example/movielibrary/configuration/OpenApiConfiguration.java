package com.example.movielibrary.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Movie Library API",
                version = "1.0",
                description = """
                        REST API for managing a movie library.

                        Users can register and log in, browse movies, and—as
                        administrators—create, replace, and delete movies.
                        Missing movie metadata is enriched asynchronously
                        through the OMDb API.
                        """,
                contact = @Contact(
                        name = "Aleksandar Oynakov",
                        url = "https://github.com/AleksandarOynakov"
                )
        ),
        servers = @Server(
                url = "http://localhost:8080",
                description = "Local development server"
        )
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Enter your Movie Library username and password"
)

public class OpenApiConfiguration {
}
