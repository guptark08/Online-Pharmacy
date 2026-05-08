package com.pharmacy.gateway;

import com.pharmacy.gateway.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayOpenApiConfigTest {

    @Test
    void gatewayOpenAPI_ShouldExposeBearerSecurity() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.gatewayOpenAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes())
            .containsKey("bearerAuth");
        SecurityScheme scheme =
            openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(openAPI.getSecurity())
            .contains(new SecurityRequirement().addList("bearerAuth"));
    }
}
