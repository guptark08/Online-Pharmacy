package com.pharmacy.eureka;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class PharmacyEurekaMainTest {

    @Test
    void constructor_ShouldInstantiate() {
        assertNotNull(new PharmacyEurekaApplication());
    }

    @Test
    void main_ShouldDelegateToSpringApplication() {
        String[] args = {"--test"};

        try (MockedStatic<SpringApplication> springApplication =
                     mockStatic(SpringApplication.class)) {
            PharmacyEurekaApplication.main(args);

            springApplication.verify(() ->
                SpringApplication.run(PharmacyEurekaApplication.class, args));
        }
    }
}
