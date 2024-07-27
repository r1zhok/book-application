package book.manager.controller;

import book.manager.controller.payload.NewBookPayload;
import book.manager.entity.BookEntity;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
class BookControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getBook_BookExists_ReturnsBookPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/1")
                .with(user("jon.dick").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.okJson(
                        """
                                   {
                                          "id": 1,
                                          "name": "Гарі Потер",
                                          "author": "Хуй знає",
                                          "details": "чтрт очкастий"
                                   }
                                """
                ))
        );

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/books/book"),
                        model().attribute("book", new BookEntity(
                                1L, "Гарі Потер", "Хуй знає", "чтрт очкастий")
                        )
                );
    }

    @Test
    void getBook_BookDoesNotExist_ReturnsError404Page() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/1")
                .with(user("jon.dick").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("catalogue-api/books/1")
                .willReturn(WireMock.notFound()));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Книжка не найдена")
                );
    }

    @Test
    void getBook_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/1/edit")
                .with(user("joe.daun"));

        //when
        this.mockMvc.perform(requestBuilder)
                //then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void getBookEditPage_BookExists_ReturnsBookEditPage() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/1/edit")
                .with(user("jon.dick").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.okJson(
                        """
                                   {
                                          "id": 1,
                                          "name": "не Гарі Потер",
                                          "author": "Хуй знає",
                                          "details": "чтрт очкастий"
                                   }
                                """
                ))
        );

        //when
        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/books/edit"),
                        model().attribute("book",
                                new BookEntity(1L, "не Гарі Потер", "Хуй знає", "чтрт очкастий")
                        )
                );
    }

    @Test
    void getBookPage_BookDoesNotExist_ReturnsError404Page() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/1/edit")
                .with(user("jon.dick").roles("MANAGER"));

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.notFound()));

        //when
        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Книжка не найдена")
                );
    }

    @Test
    void getBookEditPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/books/1/edit")
                .with(user("joe.daun"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void updateBook_RequestIsValid_RedirectsToBookPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/1/edit")
                .param("name", "Нова назва")
                .param("author", "Нова назва")
                .param("details", "Новий опис")
                .with(user("jon.dick").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.okJson(
                        """
                                   {
                                          "id": 1,
                                          "c_name": "Гарі Потер",
                                          "c_author": "Хуй знає",
                                          "c_details": "чтрт очкастий"
                                   }
                                """
                ))
        );

        WireMock.stubFor(WireMock.patch("/catalogue-api/books/1")
                .withRequestBody(WireMock.equalToJson(
                        """
                                   {
                                          "name": "Нова назва",
                                          "author": "Нова назва",
                                          "details": "Новий опис"
                                   }
                                """
                ))
                .willReturn(WireMock.noContent()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/books/1")
                );

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/catalogue-api/books/1"))
                .withRequestBody(WireMock.equalToJson(
                        """
                                   {
                                          "name": "Нова назва",
                                          "author": "Нова назва",
                                          "details": "Новий опис"
                                   }
                                """
                ))
        );
    }

    @Test
    void updateBook_RequestIsInvalid_ReturnsBookEditPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/1/edit")
                .param("name", "   ")
                .with(user("jon.dick").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.okJson(
                        """
                                   {
                                          "id": 1,
                                          "name": "Гарі Потер",
                                          "author": "Хуй знає",
                                          "details": "чтрт очкастий"
                                   }
                                """
                ))
        );

        WireMock.stubFor(WireMock.patch(WireMock.urlPathEqualTo("/catalogue-api/books/1"))
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
                        .withBody(""" 
                                    {"errors": ["Помилка 1", "Помилка 2", "Помилка 3"]}""")
                )
        );

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("catalogue/books/edit"),
                        model().attribute("book", new BookEntity(
                                1L, "Гарі Потер", "Хуй знає", "чтрт очкастий"
                        )),
                        model().attribute("errors", List.of("Помилка 1", "Помилка 2", "Помилка 3")),
                        model().attribute("payload", new NewBookPayload("   ", null, null))
                );

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathEqualTo("/catalogue-api/books/1"))
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
    void updateBook_BookDoesNotExist_ReturnsError404Page() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/book/1/edit")
                .param("name", "Нова назва")
                .param("author", "Новий автор")
                .param("details", "Новий опис")
                .with(user("jon.dick").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.notFound()));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Книжка не найдена")
                );
    }

    @Test
    void updateBook_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/1/edit")
                .param("name", "Нова назва")
                .param("author", "Новий автор")
                .param("details", "Новий опис")
                .with(user("joe.daun"))
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void deleteBook_BookExists_RedirectsToBooksListPage() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/1/delete")
                .with(user("jon.dick").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.okJson(
                        """
                                   {
                                          "id": 1,
                                          "c_name": "Гарі Потер",
                                          "c_author": "Хуй знає",
                                          "c_details": "чтрт очкастий"
                                   }
                                """
                ))
        );

        WireMock.stubFor(WireMock.delete("/catalogue-api/books/1")
                .willReturn(WireMock.noContent()));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/catalogue/books/list")
                );

        WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathMatching("/catalogue-api/books/1")));
    }

    @Test
    void deleteBook_BookDoesNotExist_ReturnsError404Page() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/1/delete")
                .with(user("jon.dick").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/catalogue-api/books/1")
                .willReturn(WireMock.notFound()));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Книжка не найдена")
                );
    }

    @Test
    void deleteBook_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/books/1/delete")
                .with(user("joe.daun"))
                .with(csrf());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }
}