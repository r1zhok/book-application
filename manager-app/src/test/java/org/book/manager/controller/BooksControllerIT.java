package org.book.manager.controller;

import org.book.manager.controller.payload.NewBookPayload;
import org.book.manager.entity.BookEntity;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
class BooksControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getAllBooks_BooksExists_ReturnsBooksListPage() throws Exception {
        // Given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/list")
                .with(user("jon.dick").roles("MANAGER"))
                .param("filter", "book");

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/catalogue-api/books"))
                .withQueryParam("filter", WireMock.equalTo("book"))
                .willReturn(WireMock.okJson(
                        """
                                   [
                                      {
                                          "id": 1,
                                          "name": "Гарі Потер",
                                          "author": "Хуй знає",
                                          "details": "чтрт очкастий"
                                      }
                                   ]
                                """
                ).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        );

        // When
        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/books/list"),
                        model().attribute("filter", "book"),
                        model().attribute("books", List.of(
                                new BookEntity(1L, "Гарі Потер", "Хуй знає", "чтрт очкастий")
                        ))
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/catalogue-api/books"))
                .withQueryParam("filter", WireMock.equalTo("book")));
    }

    @Test
    void getAllBooks_BooksExistsWithoutFilter_ReturnsBooksListPage() throws Exception {
        // Given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/list")
                .with(user("jon.dick").roles("MANAGER"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/catalogue-api/books"))
                .willReturn(WireMock.okJson(
                        """
                                   [
                                      {
                                          "id": 1,
                                          "name": "Гарі Потер",
                                          "author": "Хуй знає",
                                          "details": "чтрт очкастий"
                                      }
                                   ]
                                """
                ).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        );

        // When
        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/books/list"),
                        model().attribute("books", List.of(
                                new BookEntity(1L, "Гарі Потер", "Хуй знає", "чтрт очкастий")
                        ))
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/catalogue-api/books")));
    }

    @Test
    void createNewBook_UserNotAuthorized_ReturnsForbidden() throws Exception {
        // Given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/create")
                .with(user("jon.dick"));


        // When
        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void getNewBookPage_ReturnsNewBookPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/create")
                .with(user("jon.dick").roles("MANAGER"));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/books/new_book")
                );
    }

    @Test
    void getNewBookPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/create")
                .with(user("jon.dick"));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void createNewBook_RequestIsValid_RedirectsToCreatePage() throws Exception {
        // Given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/create")
                .param("name", "Нова книжка")
                .param("author", "Новий автор")
                .param("details", "Новий опис")
                .with(user("jon.dick").roles("MANAGER"))
                .with(csrf());;

        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/catalogue-api/books"))
                .withRequestBody(WireMock.equalToJson(
                        """
                                   {
                                       "name": "Нова книжка",
                                       "author": "Новий автор",
                                       "details": "Новий опис"
                                   }
                                """
                )).willReturn(created().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                """
                                    {
                                       "id": 1,
                                       "name": "Нова книжка",
                                       "author": "Новий автор",
                                       "details": "Новий опис"
                                    }
                                """
                        )
                )
        );

        // When
        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().is3xxRedirection(),
                        header().string(HttpHeaders.LOCATION, "/catalogue/books/1")
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/books"))
                .withRequestBody(WireMock.equalToJson(
                        """
                                   {
                                       "name": "Нова книжка",
                                       "author": "Новий автор",
                                       "details": "Новий опис"
                                   }
                              """
                ))
        );
    }

    @Test
    void createBook_RequestIsInvalid_ReturnsNewProductPage() throws Exception {
        // Given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/create")
                .param("name", "   ")
                .with(user("jon.dick").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/catalogue-api/books"))
                .withRequestBody(WireMock.equalToJson(
                        """
                        {
                            "name": "   ",
                            "author": null,
                            "details": null
                        }
                        """
                ))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody(
                                """
                                {
                                    "errors": ["Помилка 1", "Помилка 2", "Помилка 3"]
                                }
                                """
                        )
                )
        );

        // When
        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/books/new_book"),
                        model().attribute("payload", new NewBookPayload("   ", null, null)),
                        model().attribute("errors", List.of("Помилка 1", "Помилка 2", "Помилка 3"))
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/books"))
                .withRequestBody(WireMock.equalToJson(
                        """
                        {
                            "name": "   ",
                            "author": null,
                            "details": null
                        }
                        """
                ))
        );
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/create")
                .param("name", "Нова назва")
                .param("author", "Новий автор")
                .param("details", "Новий опис")
                .with(user("j.daniels"))
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isForbidden()
                );
    }
}