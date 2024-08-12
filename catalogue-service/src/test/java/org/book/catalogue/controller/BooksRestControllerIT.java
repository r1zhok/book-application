package org.book.catalogue.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@Testcontainers
@Sql("/db/books.sql")
@AutoConfigureMockMvc
class BooksRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.28");

    @Test
    void findAllBooks_ReturnBooksList() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/books")
                .param("filter", "хз")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                            [
                                                {"id":  1, "name":  "Гарік Потер", "author":  "хз", "details":  "норм"},
                                                {"id":  3, "name":  "..", "author":  "хз", "details":  "норм"}
                                            ]
                                        """
                        )
                );
    }

    @Test
    void findBooks_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/books")
                .with(jwt());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void createBook_RequestIsValid_ReturnsCreatedBook() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"name": "....", "author": "....", "details": "...."}
                        """
                )
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/catalogue-api/books/5"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                              {"id": 5, "name": "....", "author": "....", "details": "...."}
                                          """
                        )
                );
    }

    @Test
    void createBook_RequestIsInvalid_ReturnsProblemDetail() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"name": null, "author": null, "details": "...."}
                        """
                )
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(status().isBadRequest());
    }

    @Test
    void createBook_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {"name": null, "author": null, "details": "...."}
                        """
                )
                .locale(Locale.of("ua", "UA"))
                .with(jwt());

        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isForbidden()
                );
    }
}