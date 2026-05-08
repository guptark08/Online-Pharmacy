package com.pharmacy.gateway;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class PharmacyGatewayMainTest {

    @Test
    void main_ShouldDelegateToSpringApplication() {
        String[] args = {"--test"};

        try (MockedStatic<SpringApplication> springApplication =
                     mockStatic(SpringApplication.class)) {
            PharmacyGatewayApplication.main(args);

            springApplication.verify(() ->
                SpringApplication.run(PharmacyGatewayApplication.class, args));
        }
    }
}
