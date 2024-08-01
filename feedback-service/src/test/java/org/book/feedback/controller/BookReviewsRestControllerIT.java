package org.book.feedback.controller;

import lombok.extern.slf4j.Slf4j;
import org.book.feedback.entity.BookReviewEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@Slf4j
@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureWebTestClient
@ExtendWith(RestDocumentationExtension.class)
class BookReviewsRestControllerIT {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        this.mongoTemplate.insertAll(List.of(
                new BookReviewEntity(UUID.fromString("dcad1fa4-46ca-46aa-9455-07da949a049b"),
                        1L, 1, "...", "user-1"),
                new BookReviewEntity(UUID.fromString("43bacb82-811f-47cd-9c27-2b4df12edfd0"),
                        1L, 3, "...", "user-1"),
                new BookReviewEntity(UUID.fromString("369afcbd-b669-4c63-a6d3-2eb0d2b1ddba"),
                        1L, 5, "...", "user-1")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(BookReviewEntity.class).all().block();
    }

    @Test
    void findBookReviewsByBookId_ReturnsReviews() {
        this.webClient.mutateWith(mockJwt())
                .mutate().filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.info("========== REQUEST ==========");
                    log.info("{} {}", clientRequest.method(), clientRequest.url());
                    clientRequest.headers().forEach((header, value) -> log.info("{}: {}", header, value));
                    log.info("======== END REQUEST ========");
                    return Mono.just(clientRequest);
                }))
                .build()
                .get().uri("/feedback-api/book-reviews/by-book-id/1")
                .exchange()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("""
                        [
                              {"id": "dcad1fa4-46ca-46aa-9455-07da949a049b", "bookId": 1, "rating": 1,
                                      "review": "...", "userId": "user-1"},
                              {"id": "43bacb82-811f-47cd-9c27-2b4df12edfd0", "bookId": 1, "rating": 3,
                                       "review": "...", "userId": "user-1"},
                              {"id": "369afcbd-b669-4c63-a6d3-2eb0d2b1ddba", "bookId": 1, "rating": 5,
                                       "review": "...", "userId": "user-1"}
                         ]
                        """);
    }

    @Test
    void findBookReviewsByBookId_UserIsNotUnauthorized_ReturnsNoAuthorized() {
        this.webClient.get().uri("/feedback-api/book-reviews/by-book-id/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void createBookReview_RequestIsValid_ReturnsCreatedBookReview() {
        this.webClient.mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post().uri("/feedback-api/book-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"bookId":  1, "rating":  5, "review":"..."}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(
                        """
                        {"bookId":  1, "rating":  5, "review":"...", "userId": "user-tester"}
                        """).jsonPath("$.id").exists()
                .consumeWith(document("feedback/book_reviews/create_book_review",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("bookId").type("long").description("Ідентифікатор рев'ю для книжки"),
                                fieldWithPath("rating").type("int").description("Оціека книжки"),
                                fieldWithPath("review").type("string").description("Рев'ю книжки")
                        ),
                        responseFields(
                                fieldWithPath("id").type("uuid").description("Ідентифікатор рев'ю"),
                                fieldWithPath("bookId").type("long").description("Ідентифікатор книжки"),
                                fieldWithPath("rating").type("int").description("Оціека"),
                                fieldWithPath("review").type("string").description("Відгук"),
                                fieldWithPath("userId").type("string").description("Ідентифікатор користувача")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION)
                                        .description("Посилання на створений відгук книжки")
                        )
                ));
    }

    @Test
    void createBookReview_RequestIsInvalid_ReturnsBadRequest() {
        this.webClient.mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post().uri("/feedback-api/book-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"bookId":  null, "rating":  5, "review":"..."}
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .json("""
                        {
                            "errors": [
                                "Індифікатор книжки не має бути пустим"
                            ]
                        }""");
    }

    @Test
    void createBookReview_UserIsNotUnauthorized_ReturnsNoAuthorized() {
        this.webClient.post().uri("/feedback-api/book-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"bookId":  1, "rating":  5, "review":"..."}
                        """)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}