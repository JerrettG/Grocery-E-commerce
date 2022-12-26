package com.gonsalves.grocery.frontend.frontend.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;


@Configuration
@Profile("local")
public class LocalDevConfig {

    @SneakyThrows
    public LocalDevConfig(final TemplateEngine templateEngine) {
        File sourceRoot = new ClassPathResource("application.properties").getFile().getParentFile();
        while (sourceRoot.listFiles((dir, name) -> name.equals("mvnw")).length != 1) {
            sourceRoot = sourceRoot.getParentFile();
        }
        final FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setPrefix(sourceRoot.getPath() + "/src/main/resources/templates/");
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setCheckExistence(true);

        templateEngine.setTemplateResolver(fileTemplateResolver);
    }

}
