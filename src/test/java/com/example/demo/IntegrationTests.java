package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Slf4j
public class IntegrationTests {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    ObjectMapper objectMapper;

    @Value("${auth0.audience}")
    private String audience;
    
    private String token;
    
    @BeforeEach
    public void setup() {
        RestAssured.port = this.port;
    }
    
    @Test
    public void getAllPosts() throws Exception {
        //@formatter:off
         given()

            .accept(ContentType.JSON)

        .when()
            .get("/posts")

        .then()
            .assertThat()
            .statusCode(HttpStatus.SC_OK);
         //@formatter:on
    }


    private static BearerTokenRequestPostProcessor bearerToken(String token) {
        return new BearerTokenRequestPostProcessor(token);
    }

    private static class BearerTokenRequestPostProcessor implements RequestPostProcessor {

        private String token;

        BearerTokenRequestPostProcessor(String token) {
            this.token = token;
        }

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.addHeader("Authorization", "Bearer " + this.token);
            return request;
        }

    }
}