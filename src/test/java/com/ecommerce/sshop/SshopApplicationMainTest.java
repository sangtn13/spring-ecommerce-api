package com.ecommerce.sshop;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class SshopApplicationMainTest {

    @Test
    void main_DelegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            SshopApplication.main(new String[] { "arg1" });
            springApplication.verify(() -> SpringApplication.run(eq(SshopApplication.class), any(String[].class)));
        }
    }
}

