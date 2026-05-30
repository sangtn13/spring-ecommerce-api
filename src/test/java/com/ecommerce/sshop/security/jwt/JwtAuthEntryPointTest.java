package com.ecommerce.sshop.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class JwtAuthEntryPointTest {

    @InjectMocks
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    @Test
    @DisplayName("Check the structure of the Unauthorized error log written by JwtAuthEntryPoint into HttpServletResponse")
    void commence_WritesJsonErrorResponse() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException authException = new AuthenticationException("Access Denied") {};

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(outputStream));

        jwtAuthEntryPoint.commence(request, response, authException);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String jsonResult = outputStream.toString();
        assertTrue(jsonResult.contains("Unauthorized"));
        assertTrue(jsonResult.contains("You need to login to access this action"));
    }
}