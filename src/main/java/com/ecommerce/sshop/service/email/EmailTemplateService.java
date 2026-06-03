package com.ecommerce.sshop.service.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class EmailTemplateService {
    public String render(String classpathLocation, Map<String, String> placeholders) {
        try {
            String template = StreamUtils.copyToString(
                    new ClassPathResource(classpathLocation).getInputStream(),
                    StandardCharsets.UTF_8);

            String rendered = template;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return rendered;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load email template: " + classpathLocation, exception);
        }
    }
}
