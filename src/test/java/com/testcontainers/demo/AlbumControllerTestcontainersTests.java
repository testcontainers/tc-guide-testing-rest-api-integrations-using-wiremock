package com.testcontainers.demo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class AlbumControllerTestcontainersTests {

  @LocalServerPort
  private Integer port;

  @Container
  static WireMockContainer wiremockServer = new WireMockContainer(
    "wiremock/wiremock:3.3.1"
  )
    .withMapping(
      "photos-by-album",
      AlbumControllerTestcontainersTests.class,
      "mocks-config.json"
    )
    .withFileFromResource(
      "album-photos-response.json",
      AlbumControllerTestcontainersTests.class,
      "album-photos-response.json"
    );

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("photos.api.base-url", wiremockServer::getBaseUrl);
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  void shouldGetAlbumById() {
    Long albumId = 1L;

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/api/albums/{albumId}", albumId)
      .then()
      .statusCode(200)
      .body("albumId", is(albumId.intValue()))
      .body("photos", hasSize(2));
  }

  @Test
  void shouldReturnServerErrorWhenPhotoServiceCallFailed() {
    Long albumId = 2L;

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/api/albums/{albumId}", albumId)
      .then()
      .statusCode(500);
  }

  @Test
  void shouldReturnEmptyPhotos() {
    Long albumId = 3L;

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/api/albums/{albumId}", albumId)
      .then()
      .statusCode(200)
      .body("albumId", is(albumId.intValue()))
      .body("photos", hasSize(0));
  }
}
