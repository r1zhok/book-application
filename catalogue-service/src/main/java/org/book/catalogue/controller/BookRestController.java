package org.book.catalogue.controller;

import org.book.catalogue.controller.payload.NewBookPayload;
import org.book.catalogue.entity.BookEntity;
import org.book.catalogue.service.BooksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("catalogue-api/books/{bookId:\\d+}")
public class BookRestController {

    private final BooksService service;

    @ModelAttribute("book")
    public BookEntity getBook(@PathVariable("bookId") Long bookId) {
        return this.service.findBook(bookId)
                .orElseThrow(() -> new NoSuchElementException("catalogue.errors.product.not_found"));
    }

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
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            headers = @Header(name = "Content-Type", description = "Тип даних"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "exception",
                                                                    value = String.class)
                                                    }
                                            )
                                    )
                            }
                    )
            }
    )
    public BookEntity findProduct(@ModelAttribute("book") BookEntity book) {
        return book;
    }

    @PatchMapping
    @Operation(
            summary = "Оновлює дані книги",
            description = "Оновлює інформацію про книгу за заданим ідентифікатором книги. Для оновлення потрібні дані, що передаються в тілі запиту.",
            security = @SecurityRequirement(name = "keycloak"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Успішне оновлення даних книги"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Помилка валідації запиту",
                            content = @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(
                                            type = "object",
                                            properties = {
                                                    @StringToClassMapItem(key = "exception",
                                                            value = String.class)
                                            }
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "Ідентифікатор книги, що оновлюється")
            @PathVariable("bookId") Long bookId,
            @Valid @RequestBody NewBookPayload payload,
            BindingResult bindingResult
    ) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            this.service.updateBook(bookId, payload.name(), payload.author(), payload.details());
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(@PathVariable("bookId") Long bookId) {
        this.service.deleteProduct(bookId);
        return ResponseEntity.noContent()
                .build();
    }
}
