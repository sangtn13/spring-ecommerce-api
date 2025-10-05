package com.ecommerce.sshop.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(contact = @Contact(name = "SShop API", email = "admin@sshop.com", url = "https://github.com/SangTran13/spring-ecommerce-api"), description = "OpenAPI documentation for SShop eCommerce API", title = "SShop eCommerce API", version = "1.0.0", license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT"), termsOfService = "Terms of service"), servers = {
        @Server(description = "Local ENV", url = "http://localhost:5050"),
        @Server(description = "PROD ENV", url = "https://sshop.com")
}, security = {
        @SecurityRequirement(name = "bearerAuth")
})
@SecurityScheme(name = "bearerAuth", description = "JWT auth description", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class SwaggerConfig {
}