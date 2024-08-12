package org.book.catalogue.controller;

import org.book.catalogue.controller.payload.NewBookPayload;
import org.book.catalogue.entity.BookEntity;
import org.book.catalogue.service.BooksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/books")
public class BooksRestController {

    private final BooksService service;

    @GetMapping
    @Operation(
            security = @SecurityRequirement(name = "keycloak"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "id", value = Long.class),
                                                            @StringToClassMapItem(key = "name", value = String.class),
                                                            @StringToClassMapItem(key = "author", value = String.class),
                                                            @StringToClassMapItem(key = "details", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public Iterable<BookEntity> findBooks(@RequestParam(name = "filter", required = false) String filter) {
        return this.service.findAllBooks(filter);
    }

    @PostMapping
    @Operation(
            security = @SecurityRequirement(name = "keycloak"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    type = "object",
                                    properties = {
                                            @StringToClassMapItem(key = "name", value = String.class),
                                            @StringToClassMapItem(key = "author", value = String.class),
                                            @StringToClassMapItem(key = "details", value = String.class),
                                    }
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "id", value = Long.class),
                                                            @StringToClassMapItem(key = "name", value = String.class),
                                                            @StringToClassMapItem(key = "author", value = String.class),
                                                            @StringToClassMapItem(key = "details", value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public ResponseEntity<?> createBook(@Valid @RequestBody NewBookPayload payload,
                                           BindingResult bindingResult,
                                           UriComponentsBuilder uriComponentsBuilder)
            throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            BookEntity book = this.service.createBook(payload.name(), payload.author(), payload.details());
            return ResponseEntity
                    .created(uriComponentsBuilder
                            .replacePath("/catalogue-api/books/{bookId}")
                            .build(Map.of("bookId", book.getId())))
                    .body(book);
        }
    }
}
