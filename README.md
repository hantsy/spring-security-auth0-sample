# Spring Security 6 and Auth0 Integration sample

![build](https://github.com/hantsy/spring-security-auth0-sample/workflows/build/badge.svg)

## What is this?

This is a sample project demos how to use Auth0 IDP service to protect the RESTful APIs written in Spring WebMVC.

> If you are interested in the custom JWT authentication with the Spring WebMvc stack, check [spring-webmvc-jwt-sample](https://github.com/hantsy/spring-webmvc-jwt-sample/) for more details.

> If you are interested in the custom JWT authentication with the Spring WebFlux stack, check [spring-reactive-jwt-sample](https://github.com/hantsy/spring-reactive-jwt-sample/) for more details.

## Guide

* [Secures RESTful APIs with Spring Security 5 and Auth0](./docs/api.md)

## Prerequisites

Make sure you have installed the following software.

* Java 17 +
* Apache Maven 3.x
* Docker

## Build 

Clone the source codes from Github.

```bash
git clone https://github.com/hantsy/spring-security-auth0-sample
```

Open a terminal, and switch to the root folder of the project, and run the following command to build the whole project.

```bash
docker-compose up postgres // start up a postgres
mvn clean install // build the project
```

Run the application.

```bash
mvn spring-boot:run
// or from command line after building
java -jar target/xxx.jar
```


## Contribution

Any suggestions are welcome, filing an issue or submitting a PR is also highly recommended.  



## References

* [The Complete Guide to Angular User Authentication with Auth0](https://auth0.com/blog/complete-guide-to-angular-user-authentication/)
* [Quickstarts: Single-Page App/Angular](https://auth0.com/docs/quickstart/spa/angular)
