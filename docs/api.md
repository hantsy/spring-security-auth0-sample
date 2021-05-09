# Secures RESTful APIs with Spring Security 5 and Auth0 

Spring Security 5  deprecated the original Spring Security OAuth module and rewrote the OAuth2/Oidc implementation, make it available as part of the core modules. 

OAuth protocol defines 4 roles, including *Resource Owner*, *Client*, *Authorization server* and *Resource server*. Imagine you are using an app in your mobile, you(*Resource owner*) authorizes the app(presents a *Client* role itself) to perform an authorization flow to get access token from the authorization server, then utilizes this token to interact with the protected resources(from *Resource server*).

Oidc is an additional supplement to enrich OAuth2 protocol. In Spring Security 5, it does not differentiate them explicitly in the API level and configuration. And nowdays most OAuth2 compatible provider supports the Oidc protocol.

> If you are new to OAuth2 and OIDC, please check the official [OAuth 2 docs](https://oauth.net/2/) and explore [OpenId Connect](https://openid.net/connect/) related specs. For impatient users, [this introductory article](https://www.digitalocean.com/community/tutorials/an-introduction-to-oauth-2) from DigitalOcean is a good start point.

With Spring Security 5, it is easy to set up OAuth2 *Client* and *Resource server* roles in your applications.  And there is [another incubator project](https://github.com/spring-projects-experimental/spring-authorization-server) which allow you to serve a custom *Authorization server*. In a real world application, we can select a mature IDP service to simplify the integration work of multiple authentication approaches, for example, you want to integrate Google accounts, Facebook, Twitter  and custom username/password authentication in your applications. [Auth0](https://www.auth0.com) and [Okta](https://www.okta.com) are the leading IDP services in this area, they provide excellent administrative UI and rich SDK of all use case in your applications. In the open source world, [Keycloak](https://www.keycloak.org/) is wildly used to host a custom authentication server.

> From the official Okta blog,  Auth0 has joined the Okta family. I am a little afraid Auth0 will be disappeared like Stormpath. But the Okta guys declared that Auth0 will operate as a standalone brand.

In this post, we will use Auth0 as the authentication server role to simplify our development progress. 

To demonstrate the OAuth2 flow, firstly we will create a RESTful API *application* that acts as *Resource server*. In the further post, we will create an Angular SPA client app to interact with this API backend.  

There are plenty of tutorials provided on the official [auth0 docs page](https://auth0.com/docs/). Usually you can find the use cases matched your applications in these docs,  go to the *Backend API*  doc to go through the tutorial, esp. there is a [great tutorial for Spring Security 5](https://auth0.com/docs/quickstart/backend/java-spring-security5).

Firstly register an Auth0 account if you don't have one before. Or login using a social network account directly. After logged in, you will find Auth0 has created a development-purpose domain for you, it categories all resources for your new application.

In the dashboard UI, expand the *Applications/APIs* in the left pane, let's create a new API application(*Resource server* role in the OAuth2 protocol).
* Click the *Create API* button to start the progress.
* Fill the essential fields, the *audience* field is used to identify this API, it is recommended to fill a  URL like value. For example. Here I created a new API application named *Backend API*, and set the audience to *https://hantsy.github.io/api*.

Auth0 also provides an *Auth0 Management API* for you to access the Auth0 APIs, and for test purpose, Auth0 also created a *Machine to Machine* application automatically which allow you to interact with your own APIs.

Next let's create our *Backend API* application. Go to https://start.spring.io, add the following dependencies,

* Web Starter
* Data JPA
* Spring Security
* Lombok

Generate the project archive, extract the files into your disk.
Import the project into IDEA, open the *pom.xml* file, add spring security oauth2 related dependencies.

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

In this post, we are going to integrate Auth0 with Spring Security to protect the RESTful APIs, and we will focus on the security configuration for an API application. To simplify the development work, I copied the existing codes from [hantsy/spring-webmvc-functional-sample](https://github.com/hantsy/spring-webmvc-functional-sample), which is used to produce RESTful APIs for a simple blog application.

> For the introduction of Spring WebMvc functional programming, check [my post published on Medium](https://hantsy.medium.com/functional-programming-in-spring-mvc-20957653da1).

Declares a `SecurityFilterChain` bean to configure the details of Spring Security framework.

```java
@Bean
SecurityFilterChain springWebFilterChain(HttpSecurity http) throws Exception {
    return http
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeRequests(c -> c
                           .antMatchers("/", "/info").permitAll()
                           .antMatchers(HttpMethod.GET, "/posts/**").permitAll()//.hasAuthority("SCOPE_read:posts")
                           .antMatchers(HttpMethod.POST, "/posts/**").hasAuthority("SCOPE_write:posts")
                           .antMatchers(HttpMethod.PUT, "/posts/**").hasAuthority("SCOPE_write:posts")
                           .antMatchers(HttpMethod.DELETE, "/posts/**").hasAuthority("SCOPE_delete:posts")
                           .anyRequest().authenticated()
                          )
        .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
        .build();
}
```

1. The `oauth2ResourceServer` indicates this application will work as a *Resource server* role,  and it uses a `JWT` token to validate the incoming request.
2. The `authorizeRequests` section defines the security rules againt HTTP URI and verbs,  eg. requires a  `write:posts` permission when creating new posts.

Unlike the *opaque* token, a JWT token can be simply validated by a local public key or JWK set uri prvided by the authorization server. 

In Spring Security 5, a JWT token can be decoded from a configured *issurer_uri* if the auth provider implements OpenId Configuration spec, and exposes the whole OAuth2/Oidc configuration metadata such as token url, authorization server url, jwk set uri, user info uri etc. through a single url `.well-known/openid-configuration` .  

Add the following configuration in the *application.properties*.

```java
 spring:
  ...
  security:
    oauth2:
      resourceserver:
        jwt:
          # Replace with the domain of your Auth0 tenant.
          # Note the trailing slash is important!
          issuer-uri: https://dev-ese8241b.us.auth0.com/
```

To check if the audience is set and included in the access token, add a custom audience validator.

 ```java
 @RequiredArgsConstructor(staticName = "of")
 public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
     private final String audience;
     
     public OAuth2TokenValidatorResult validate(Jwt jwt) {
         OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);
         
         if (jwt.getAudience().contains(audience)) {
             return OAuth2TokenValidatorResult.success();
         }
         
         return OAuth2TokenValidatorResult.failure(error);
     }
 }
 ```

And add this validator in the `JwtDecoder` bean.

```java
@Bean
JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties, @Value("${auth0.audience}") String audience) {
    /*
        By default, Spring Security does not validate the "aud" claim of the token, to ensure that this token is
        indeed intended for our app. Adding our own validator is easy to do:
        */

    String issuerUri = properties.getJwt().getIssuerUri();
    NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> audienceValidator = AudienceValidator.of(audience);
    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
}
```

Next, let's add some permissions configuration in the Auth0 administration dashboard.

Go to the *Settings* page of *Backend API*, and click the *Permissions* tab, add the following permissions.

* *read:posts* - Read posts
* *write:posts* - Create and update Posts
* *delete:posts* - Delete posts

Switch to *Machine to Machine applications* tab, make sure the *Test application* is enabled, and expand the configuration of this  application, make sure all permissions are checked on, the hit the **Update** button to apply the changes.

Now switch to the *Test* tab, there is a guide for testing your API application with token, it is available in different scripts. Just copy the **cURL** command, and open a Unix/bash like terminal, and paste and execute it.

```bash
$ curl --request POST \
>   --url https://dev-ese8241b.us.auth0.com/oauth/token \
>   --header 'content-type: application/json' \
>   --data '{"client_id":"IEXV2MbAiuEkV0J7ufH0Bqq2a2YIF3h1","client_secret":"OknFJUkFpBe_tpFPAP2hL10NliMbCGxMRiFH_DkTxylBnZ5WjP9aq6zQOtL2L7ea","audience":"https://hantsy.github.io/api","grant_type":"client_credentials"}'
{"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlYzM1lvNzk5cC1XeFI2NHpJZ29QMyJ9.eyJpc3MiOiJodHRwczovL2Rldi1lc2U4MjQxYi51cy5hdXRoMC5jb20vIiwic3ViIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDFAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vaGFudHN5LmdpdGh1Yi5pby9hcGkiLCJpYXQiOjE2MjA1NDgzMjAsImV4cCI6MTYyMDYzNDcyMCwiYXpwIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDEiLCJzY29wZSI6InJlYWQ6cG9zdHMgd3JpdGU6cG9zdHMgZGVsZXRlOnBvc3RzIiwiZ3R5IjoiY2xpZW50LWNyZWRlbnRpYWxzIn0.UCdmcIb8xREC_rQp3EVBx8EALzhPcpJL1Mh1n6UX-Sf2cHkSsiUFISkHHM-qa5qZ34aNxJwKfvKPnZsfcIH9xwaGMeZjHub22PwAyVscQpZBq2mPRe_28GiwMP9jtFmSPhuGQec6-K7f0410tZTEKggnX3qFDdsJBWEhnu4zeFSwn865--Lbgk52owdncMd8jp2O5olKdnkeieChGQaYsLQ_vM8JTDN-eu5hm1DEL8WcfCd_foIG6klcm_gzgHpAru6zJQb48OmWIxlL6VGQl7RqIQIfds2NBBG7xYVr_38M2cltx8EUyIcXedR-d4YG_Dfp-NJbFkpZcSs9PPTagw","scope":"read:posts write:posts delete:posts","expires_in":86400,"token_type":"Bearer"}
```

Copy to the access_token, open https://jwt.io page, and paste the token in the *Encode* box, you can see the decoded result.

![jwt.io](./jwtio.png)



Now let's use this token to access the protected APIs in our *Backend API* application.  

Firstly start up a Postgres server. Simply run the following docker command.

```bash
$ docker compose up postgres
```

Then run the application.

```bash
$ mvn clean spring-boot:run
// or 
$ mvn clean package
$ java -jar ./target/xxx.jar
```

Try to create a new post.

```bash
$ curl --request POST \
  --url http://localhost:8080/posts \
  --header 'authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlYzM1lvNzk5cC1XeFI2NHpJZ29QMyJ9.eyJpc3MiOiJodHRwczovL2Rldi1lc2U4MjQxYi51cy5hdXRoMC5jb20vIiwic3ViIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDFAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vaGFudHN5LmdpdGh1Yi5pby9hcGkiLCJpYXQiOjE2MjA1NDgwNzEsImV4cCI6MTYyMDYzNDQ3MSwiYXpwIjoiSUVYVjJNYkFpdUVrVjBKN3VmSDBCcXEyYTJZSUYzaDEiLCJzY29wZSI6InJlYWQ6cG9zdHMgd3JpdGU6cG9zdHMgZGVsZXRlOnBvc3RzIiwiZ3R5IjoiY2xpZW50LWNyZWRlbnRpYWxzIn0.Rg6UqIb2kjrPg39QRLOLsvI54TUsQM4B7j_6Eb4hlxFkzHWglD-V0b5kXfgSMIcBQ-fq5oLt7Fj8etgqMMpstyx-2Ob20rbCBd-msZ1o-xGQidkQsyEM6lL6GMX7d79Vw0H9eZN8D8j8_mVoqd-wENhWsBiSp3xq8Jz6KSdslrxGnMaQKbQ2IL7dgw8Y1kJBz8FgBcUUF5k-dhMB-DDLkaLsurwcb380Qk3gMD2JW97fxQWM_mi38ufcoopSWxznDE118ng7hQXT18vFB9N_RCNaPHslsKZsE6ytiwx6kiSYvGFN5RMWIa3Dyp6Rt4UjfTLx1WO9JDtxNc4NMSPQ-g' \
  --header 'Content-Type: application/json' \
  --data '{"title": "New Post about Spring Secrity and Auth0", "content": "Content of this post, TBD"}'
```

Then list all posts and check if the post is created as expected.

```bash
$ curl http://localhost:8080/posts
[ {
  "id" : 1,
  "version" : 0,
  "createdDate" : "2021-05-09T16:37:11.984273",
  "lastModifiedDate" : "2021-05-09T16:37:11.984273",
  "title" : "Post one",
  "content" : "content of Post one"
}, {
  "id" : 2,
  "version" : 0,
  "createdDate" : "2021-05-09T16:37:12.068004",
  "lastModifiedDate" : "2021-05-09T16:37:12.068004",
  "title" : "Post two",
  "content" : "content of Post two"
}, {
  "id" : 3,
  "version" : 0,
  "createdDate" : "2021-05-09T16:38:27.881024",
  "lastModifiedDate" : "2021-05-09T16:38:27.881024",
  "createdBy" : {
    "username" : "IEXV2MbAiuEkV0J7ufH0Bqq2a2YIF3h1@clients"
  },
  "lastModifiedBy" : {
    "username" : "IEXV2MbAiuEkV0J7ufH0Bqq2a2YIF3h1@clients"
  },
  "title" : "New Post about Spring Secrity and Auth0",
  "content" : "Content of this post, TBD"
} ]
```

The *Backend API* is ready for being accessed by client applications.  Now let's explore the testing facilities provided in the Spring Security Test module to test the APIs protected by a JWT token.

Spring Security provides a simple `SecurityMockMvcRequestPostProcessors.jwt` to mock JWT processing and bypass the JWT decoding in a Spring MockMvc environment.

```java

@Test
public void testSave() throws Exception {

    given(this.posts.save(any(Post.class)))
        .willReturn(Post.builder().title("test").content("test content").build());

    this.mockMvc
        .perform(
        post("/posts")
        .with(jwt().jwt(jwtBuilder().claim("scope", "write:posts").build()))
        .content(this.objectMapper.writeValueAsBytes(Post.builder().title("test").content("test content").build()))
        .contentType(MediaType.APPLICATION_JSON)
    )
        .andExpect(status().isCreated());

    verify(this.posts, times(1)).save(any(Post.class));
    verifyNoMoreInteractions(this.posts);
}

private Jwt.Builder jwtBuilder() {
    return Jwt.withTokenValue("token").header("alg", "none").audience(List.of(audience));
}
```

In an integration test, you can use the same access token generated from the *Test application* to interact with the *Backend API*. 

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Slf4j
public class IntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${auth0.audience}")
    private String audience;

    private String token = "...";
    
    @BeforeEach
    public void setup() {
        RestAssured.port = this.port;
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
```

Grab a copy of the source codes from [my Github](https://github.com/hantsy/spring-security-auth0-sample/tree/master/api).
