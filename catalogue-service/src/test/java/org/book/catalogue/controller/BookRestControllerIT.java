package org.book.catalogue.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.HeadersModifyingOperationPreprocessor;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Locale;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
class BookRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.28");

    @Test
    @Sql("/db/books.sql")
    void findProduct_BookExists_ReturnsProductsList() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/books/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                        {"id":  1, "name":  "Гарік Потер", "author": "хз", "details": "норм"}
                                        """
                        )
                ).andDo(document("catalogue/books/find_all",
                        preprocessResponse(prettyPrint(),
                                new HeadersModifyingOperationPreprocessor().remove("Vary")),
                        responseFields(
                                fieldWithPath("id").description("Ідентифікатор книжки").type("long"),
                                fieldWithPath("name").description("Назва книжки").type("string"),
                                fieldWithPath("author").description("Автор книжки").type("string"),
                                fieldWithPath("details").description("Опис книжки").type("string")
                        )
                ));
    }

    @Test
    void findBook_BookDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/books/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    @Sql("/db/books.sql")
    void findBook_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/books/1")
                .with(jwt());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql("/db/books.sql")
    void updateBook_RequestIsValid_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                        {"name":  "idk", "author":  "idk", "details": "idk"}
                                """
                )
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNoContent()

                );
    }

    @Test
    @Sql("/db/books.sql")
    void updateBook_RequestIsInvalid_ReturnsBadRequest() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                        {"name":  null, "author":  null, "details": "idk"}
                                """
                )
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest()
                );
    }

    @Test
    void updateBook_BookDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/books/1")
                .locale(Locale.of("ua"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                        {"name":  "idk", "author":  "idk", "details": "idk"}
                                """
                )
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    void updateBook_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.patch("/catalogue-api/books/1")
                .locale(Locale.of("ua"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                       {"name":  "idk", "author":  "idk", "details": "idk"}
                                """
                )
                .with(jwt());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql("/db/books.sql")
    void deleteBook_BookExists_ReturnsNoContent() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/books/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void deleteBook_BookDoesNotExist_ReturnsNotFound() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/books/1")
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    void deleteBook_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.delete("/catalogue-api/books/1")
                .with(jwt());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }
}