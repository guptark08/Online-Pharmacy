package com.pharmacy.eureka;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class PharmacyEurekaApplicationTests {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            PharmacyEurekaApplication.main(new String[0]);

            springApplication.verify(
                () -> SpringApplication.run(PharmacyEurekaApplication.class, new String[0]));
        }
    }
}
