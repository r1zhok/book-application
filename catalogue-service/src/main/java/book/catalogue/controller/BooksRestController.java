package book.catalogue.controller;

import book.catalogue.controller.payload.NewBookPayload;
import book.catalogue.entity.BookEntity;
import book.catalogue.service.BooksService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public Iterable<BookEntity> findBooks(@RequestParam(name = "filter", required = false) String filter) {
        return this.service.findAllBooks(filter);
    }

    @PostMapping
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
