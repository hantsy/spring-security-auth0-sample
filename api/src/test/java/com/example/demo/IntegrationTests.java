package com.example.demo;

import com.example.demo.domain.Post;
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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


//  From Test tab in your APIs.
//
// curl --request POST \
//         >   --url https://dev-ese8241b.us.auth0.com/oauth/token \
//         >   --header 'content-type: application/json' \
//         >   --data '{"client_id":"XXX","client_secret":"XXX","audience":"https://hantsy.github.io/api","grant_type":"client_credentials"}'
//         {"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlYzM1lvNzk5cC1XeFI2NHpJZ29QMyJ9.eyJpc3MiOiJodHRwczovL2Rldi1lc2U4MjQxYi51cy5hdXRoMC5jb20vIiwic3ViIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDFAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vaGFudHN5LmdpdGh1Yi5pby9hcGkiLCJpYXQiOjE2MTkzNDAyODUsImV4cCI6MTYxOTQyNjY4NSwiYXpwIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDEiLCJzY29wZSI6InJlYWQ6cG9zdHMgd3JpdGU6cG9zdHMgZGVsZXRlOnBvc3RzIiwiZ3R5IjoiY2xpZW50LWNyZWRlbnRpYWxzIn0.pHoj6txIAkS14QCbRJ7azp6JjfUwaT2YZrznW4CmaKoYfS_Ibi0pg-fkIhgbvTHge_2mZSYhNn76lJA8IM2A0YPRffCYmNRMXhfuhjjP1QyCdiz6u6A8nKc40cN9SG573oOC3-qHaw5jROANRyZPpAY8MEZw3HSQGYAtJ6XY_F-MoJwILv56Ah2paqcU-qhCsX5XHscxI5e1xPj3AygBOHlrKZDZMbffMZK_m3nDP5iF_I7EKsIzCS7SAiZJrSJvFSyMHz2iCkVjnB1kX8FZFItTg7YfdF2D7of2ekKR43haljoaYEtXKfoiQAdThhzHCbZ0zyrFH9zLIicxqjWDaQ","scope":"read:posts write:posts delete:posts","expires_in":86400,"token_type":"Bearer"}

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Slf4j
public class IntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${auth0.audience}")
    private String audience;

    private String token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlYzM1lvNzk5cC1XeFI2NHpJZ29QMyJ9.eyJpc3MiOiJodHRwczovL2Rldi1lc2U4MjQxYi51cy5hdXRoMC5jb20vIiwic3ViIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDFAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vaGFudHN5LmdpdGh1Yi5pby9hcGkiLCJpYXQiOjE2MTkzNDAyODUsImV4cCI6MTYxOTQyNjY4NSwiYXpwIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDEiLCJzY29wZSI6InJlYWQ6cG9zdHMgd3JpdGU6cG9zdHMgZGVsZXRlOnBvc3RzIiwiZ3R5IjoiY2xpZW50LWNyZWRlbnRpYWxzIn0.pHoj6txIAkS14QCbRJ7azp6JjfUwaT2YZrznW4CmaKoYfS_Ibi0pg-fkIhgbvTHge_2mZSYhNn76lJA8IM2A0YPRffCYmNRMXhfuhjjP1QyCdiz6u6A8nKc40cN9SG573oOC3-qHaw5jROANRyZPpAY8MEZw3HSQGYAtJ6XY_F-MoJwILv56Ah2paqcU-qhCsX5XHscxI5e1xPj3AygBOHlrKZDZMbffMZK_m3nDP5iF_I7EKsIzCS7SAiZJrSJvFSyMHz2iCkVjnB1kX8FZFItTg7YfdF2D7of2ekKR43haljoaYEtXKfoiQAdThhzHCbZ0zyrFH9zLIicxqjWDaQ";

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

    @Test
    public void createPostWithoutToken() throws Exception {
        //@formatter:off
        given()
            .body(Post.builder().title("test").content("test content").build())
            .contentType(ContentType.JSON)
        .when()
            .post("/posts")
        .then()
            .assertThat()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);
        //@formatter:on
    }

    @Test
    public void createPost() throws Exception {
        //@formatter:off
        given()
            .auth().oauth2(token)
            .body(Post.builder().title("test").content("test content").build())
            .contentType(ContentType.JSON)
        .when()
            .post("/posts")
        .then()
            .assertThat()
            .statusCode(HttpStatus.SC_CREATED)
            .header("Location", notNullValue());
        //@formatter:on
    }

}