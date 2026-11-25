package com.sarpraskost.app.config;

import com.sarpraskost.app.service.FileStorageService;
import com.sarpraskost.app.util.SqlInjectionValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    public WebConfig(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadLocation = fileStorageService.getUploadDir().toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(
                HttpServletRequest request,
                HttpServletResponse response,
                Object handler
            ) {
                SqlInjectionValidator.validateRawQueryString(request.getQueryString());
                return true;
            }
        }).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
