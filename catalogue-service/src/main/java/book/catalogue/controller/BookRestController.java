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
    public BookEntity findProduct(@ModelAttribute("book") BookEntity book) {
        return book;
    }

    @PatchMapping
    public ResponseEntity<?> updateProduct(@PathVariable("bookId") Long bookId,
                                           @Valid @RequestBody NewBookPayload payload,
                                           BindingResult bindingResult) throws BindException {
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
