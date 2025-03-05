package com.vivero.viveroApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.vivero.viveroApp.converter.StringToDescuentoConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final StringToDescuentoConverter stringToDescuentoConverter;

    public WebConfig(StringToDescuentoConverter stringToDescuentoConverter) {
        this.stringToDescuentoConverter = stringToDescuentoConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToDescuentoConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
