package com.pharmacy.configserver;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class PharmacyConfigServerApplicationTests {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            PharmacyConfigServerApplication.main(new String[0]);

            springApplication.verify(
                () -> SpringApplication.run(PharmacyConfigServerApplication.class, new String[0]));
        }
    }
}
