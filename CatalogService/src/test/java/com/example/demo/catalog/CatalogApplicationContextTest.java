package com.example.demo.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "file.upload-dir=uploads-test"
})
class CatalogApplicationContextTest {

    @Test
    void contextLoads() {
    }
}
