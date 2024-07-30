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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class BooksControllerCustomerAppIT {

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        stubFor(get(urlPathMatching("/catalogue-api/books"))
                .willReturn(okJson("""
                        [
                            {
                                "id": 1,
                                "name": "Книжка 1",
                                "author": "хз",
                                "details": "Опис книжки №1"
                            },
                            {
                                "id": 2,
                                "name": "Книжка 2",
                                "author": "хтось",
                                "details": "Опис книжки №2"
                            },
                            {
                                "id": 3,
                                "name": "Книжка 3",
                                "author": "хз",
                                "details": "Опис книжки №3"
                            }
                        ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                ));
    }

    @Test
    void getBooksList_ReturnBooksList() {
        this.webTestClient.mutateWith(mockUser())
                .get()
                .uri("/customer/books/list")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getBooksList_UserIsNotAuthorized_RedirectsToLoginPage() {
        this.webTestClient
                .get()
                .uri("/customer/books/list")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @Test
    void getFavouriteBooksPage_ReturnFavouriteBooks() {
        stubFor(get(urlPathMatching("/feedback-api/favourite-books"))
                .willReturn(okJson("""
                        [
                            {
                                "id": "a16f0218-cbaf-11ee-9e6c-6b0fa3631587",
                                "bookId": 1,
                                "userId": "2051e72a-cbca-11ee-8e8b-a3841adf45d0"
                            },
                            {
                                "id": "a42ff37c-cbaf-11ee-8b1d-cb00912914b5",
                                "bookId": 3,
                                "userId": "2051e72a-cbca-11ee-8e8b-a3841adf45d0"
                            }
                        ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                ));

        this.webTestClient.mutateWith(mockUser())
                .get().uri("/customer/books/favourites")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getFavouriteBooksPage_UserIsNotAuthorized_RedirectsToLoginPage() {
        this.webTestClient
                .get()
                .uri("/customer/books/favourites")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }
}