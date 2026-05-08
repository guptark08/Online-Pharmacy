package com.pharmacy.configserver;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class PharmacyConfigServerMainTest {

    @Test
    void constructor_ShouldInstantiate() {
        assertNotNull(new PharmacyConfigServerApplication());
    }

    @Test
    void main_ShouldDelegateToSpringApplication() {
        String[] args = {"--test"};

        try (MockedStatic<SpringApplication> springApplication =
                     mockStatic(SpringApplication.class)) {
            PharmacyConfigServerApplication.main(args);

            springApplication.verify(() ->
                SpringApplication.run(PharmacyConfigServerApplication.class, args));
        }
    }
}
