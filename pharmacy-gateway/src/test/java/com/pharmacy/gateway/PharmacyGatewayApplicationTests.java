package com.pharmacy.gateway;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class PharmacyGatewayApplicationTests {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            PharmacyGatewayApplication.main(new String[0]);

            springApplication.verify(
                () -> SpringApplication.run(PharmacyGatewayApplication.class, new String[0]));
        }
    }
}
