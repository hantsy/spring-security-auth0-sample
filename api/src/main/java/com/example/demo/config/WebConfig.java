package com.example.demo.config;

import com.example.demo.web.PostHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Configuration
public class WebConfig {
    
    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postHandler, BlogProperties blogProperties) {
        return route(GET("/info"), (req) -> ok().body(blogProperties))
                .andRoute(GET("/posts"), postHandler::all)
                .andRoute(POST("/posts"), postHandler::create)
                .andRoute(GET("/posts/{id}"), postHandler::get)
                .andRoute(PUT("/posts/{id}"), postHandler::update)
                .andRoute(DELETE("/posts/{id}"), postHandler::delete);
    }

    @Bean
    @Profile("cors")
    CorsFilter corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "https://mytrustedwebsite.com"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return  new CorsFilter(source);
    }

}
