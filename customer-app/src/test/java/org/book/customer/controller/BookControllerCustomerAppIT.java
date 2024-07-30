package org.book.customer.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class BookControllerCustomerAppIT {

    @Autowired
    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        stubFor(get("/catalogue-api/books/1")
                .willReturn(okJson("""
                        {
                            "id": 1,
                            "name": "...",
                            "author": "....",
                            "details": "..."
                        }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void getBookPage_ReturnsBookPage() {
        stubFor(get("/feedback-api/book-reviews/by-book-id/1")
                .willReturn(okJson(
                        """
                                    [
                                        {
                                            "id": "595d4e5a-cbc1-11ee-864f-8fb72674ccaf",
                                            "bookId": 1,
                                            "rating": 3,
                                            "review": "...",
                                            "userId": "5da9bf2a-cbc1-11ee-a8a7-d355f5a3dd8e"
                                        },
                                        {
                                            "id": "63c4410a-cbc1-11ee-92ea-eff590e7852e",
                                            "bookId": 1,
                                            "rating": 5,
                                            "review": "...",
                                            "userId": "6b3cce0c-cbc1-11ee-ac61-b7eed6e7b4f4"
                                        }
                                    ]
                                """
                ).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        );

        stubFor(get("/feedback-api/book-favourites/by-book-id/1")
                .willReturn(created().
                        withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                """ 
                                            {
                                                "id": "2ecc74c2-cb17-11ee-b719-e35a0e241f11",
                                                "bookId": 1,
                                            }
                                        """
                        ))
        );

        this.webClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/books/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getBookPage_BookIsNotExist_ReturnsNotFound() {
        this.webClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/books/404")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getBookPage_UserIsNotAuthenticated_ReturnsLoginPage() {
        this.webClient
                .get()
                .uri("/customer/books/1")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void addBookToFavourites_RequestIsValid_ReturnsRedirectionToBookPage() {
        stubFor(post("/feedback-api/favourite-products")
                .withRequestBody(equalToJson(
                        """
                                {
                                   "bookId": 1
                                }
                            """
                ))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                """
                                    {
                                        "id": 2ecc74c2-cb17-11ee-b719-e35a0e241f11,
                                        "bookId": 1,
                                    }
                                """
                        )
                )
        );

        this.webClient.mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/1/add-to-favourites")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/books/1");

        verify(postRequestedFor(urlPathMatching("/feedback-api/favourite-books"))
                .withRequestBody(equalToJson(
                        """
                                {
                                     "bookId": 1
                                }
                             """
                ))
        );
    }

    @Test
    void addBookToFavourites_BookDoesNotExist_ReturnsNotFoundPage() {
        this.webClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/404/add-to-favourites")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addBookToFavourites_UserIsNotAuthorized_RedirectsToLoginPage() {
        this.webClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/1/add-to-favourites")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void removeBookFromFavourites_BookExists_ReturnsRedirectionToBookPage() {
        stubFor(delete("/feedback-api/favourite-books/by-book-id/1")
                .willReturn(noContent()));

        this.webClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/book/1/remove-from-favourites")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/books/1");

        verify(deleteRequestedFor(urlPathMatching("/feedback-api/favourite-books/by-book-id/1")));
    }

    @Test
    void removeBookFromFavourites_BookDoesNotExist_ReturnsNotFoundPage() {
        this.webClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/404/remove-from-favourites")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void removeBookFromFavourites_UserIsNotAuthorized_RedirectsToLoginPage() {
        this.webClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/1/remove-from-favourites")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
        void createReview_RequestIsValid_RedirectsToBookPage() {
        stubFor(post("/feedback-api/book-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "bookId": 1,
                            "rating": 3,
                            "review": "Ну, на троєчку..."
                        }"""))
                .willReturn(created()
                        .withHeader(HttpHeaders.LOCATION, "http://localhost/feedback-api/book-reviews/b852bc8e-cbc5-11ee-bbc5-bf192e2492e5")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "b852bc8e-cbc5-11ee-bbc5-bf192e2492e5",
                                    "bookId": 1,
                                    "rating": 3,
                                    "review": "Ну, на троєчку...",
                                    "userId": "1a24d4ec-cbc6-11ee-af3b-0b236022162c"
                                }""")));

        // when
        this.webClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/1/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "Ну, на троєчку..."))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        verify(postRequestedFor(urlPathMatching("/feedback-api/book-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "bookId": 1,
                            "rating": 3,
                            "review": "Ну, на троєчку..."
                        }""")));
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsBookPage() {
        stubFor(post("/feedback-api/book-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "bookId": 1,
                            "rating": -1,
                            "review": "..."
                        }"""))
                .willReturn(badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Помилка 1"]
                                }""")));

        stubFor(get("/feedback-api/favourite-books/by-book-id/1")
                .willReturn(okJson("""
                        {
                            "id": "ec586ecc-cbc8-11ee-8e7d-4fce5e860855",
                            "bookId": 1,
                            "userId": "f1177a8e-cbc8-11ee-8ca2-0bf025125fd5"
                        }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        this.webClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/1/create-review")
                .body(BodyInserters.fromFormData("rating", "-1")
                        .with("review", "..."))
                .exchange()
                .expectStatus().isBadRequest();

        verify(postRequestedFor(urlPathMatching("/feedback-api/book-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "bookId": 1,
                            "rating": -1,
                            "review": "..."
                        }""")));
    }

    @Test
    void createReview_BookDoesNotExist_ReturnsNotFoundPage() {
        this.webClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/404/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "..."))
                .exchange()
                .expectStatus().isNotFound();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/books/404")));
    }

    @Test
    void createReview_UserIsNotAuthorized_RedirectsToLoginPage() {
        this.webClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/books/1/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "..."))
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }
}