package org.book.feedback.controller;

import org.book.feedback.entity.FavouriteBookEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;


@SpringBootTest
@AutoConfigureWebTestClient
class FavouriteBooksRestControllerIT {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        this.mongoTemplate.insertAll(List.of(
                new FavouriteBookEntity(UUID.fromString("dcad1fa4-46ca-46aa-9455-07da949a049b"),
                        1L, "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"),
                new FavouriteBookEntity(UUID.fromString("43bacb82-811f-47cd-9c27-2b4df12edfd0"),
                        2L, "3c467d3c-cbda-11ee-aa43-1782cd18c42f"),
                new FavouriteBookEntity(UUID.fromString("369afcbd-b669-4c63-a6d3-2eb0d2b1ddba"),
                        3L, "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(FavouriteBookEntity.class).all().block();
    }

    @Test
    void findFavouriteBooks_ReturnsFavouriteBooks() {
        webClient.mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .get().uri("/feedback-api/favourite-books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(
                        """
                                [
                                                            {
                                                                "id": "dcad1fa4-46ca-46aa-9455-07da949a049b",
                                                                "bookId": 1,
                                                                "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                                                            },
                                                            {
                                                                "id": "369afcbd-b669-4c63-a6d3-2eb0d2b1ddba",
                                                                "bookId": 3,
                                                                "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                                                            }
                                ]
                                """
                );
    }

    @Test
    void findFavouriteBooks_UserIsNotAuthenticated_ReturnsUnauthorized() {
        webClient.get().uri("/feedback-api/favourite-books")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void findFavouriteBookByBookId_ReturnsFavouriteBook() {
        webClient.mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .get().uri("/feedback-api/favourite-books/by-book-id/3")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(
                        """
                                 {
                                      "id": "369afcbd-b669-4c63-a6d3-2eb0d2b1ddba",
                                       "bookId": 3,
                                       "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                                }
                                """);
    }

    @Test
    void findFavouriteBookByBookId_UserIsNotAuthenticated_ReturnsUnauthorized() {
        webClient.get().uri("/feedback-api/favourite-books/by-book-id/3")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void addBookToFavouriteBooks_RequestIsValid_ReturnsFavouriteBook() {
        this.webClient.mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .post().uri("/feedback-api/favourite-books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                                {
                                              "bookId": 4
                                }
                                """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(
                        """
                                {
                                       "bookId": 4,
                                       "userId": "5f1d5cf8-cbd6-11ee-9579-cf24d050b47c"
                                }
                                """
                ).jsonPath("$.id").exists();
    }

    @Test
    void addBookToFavouriteBooks_RequestIsInvalid_ReturnsBadRequestException() {
        this.webClient.mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .post().uri("/feedback-api/favourite-books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                                {
                                              "bookId": null
                                }
                                """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().json(
                        """
                                {"errors": [
                                      "Індифікатор книжки не має бути пустим"
                                ]}
                                """);
    }

    @Test
    void addBookToFavouriteBooks_UserIsNotAuthenticated_ReturnsUnauthorized() {
        webClient.post().uri("/feedback-api/favourite-books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        """
                                {
                                              "bookId": null
                                }
                                """)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void removeBookFromFavouriteBooks_ReturnNoContent() {
        webClient.mutateWith(mockJwt().jwt(builder -> builder.subject("5f1d5cf8-cbd6-11ee-9579-cf24d050b47c")))
                .delete().uri("/feedback-api/favourite-books/by-book-id/3")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void removeBookFromFavouriteBooks_UserIsNotAuthenticated_ReturnsUnauthorized() {
        webClient.delete().uri("/feedback-api/favourite-books/by-book-id/3")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}