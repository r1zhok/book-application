package org.book.customer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.book.customer.client.BookReviewsClient;
import org.book.customer.client.BooksClient;
import org.book.customer.client.FavouriteBooksClient;
import org.book.customer.client.exeption.ClientBadRequestException;
import org.book.customer.controller.payload.NewBookReviewPayload;
import org.book.customer.entity.BookEntity;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("customer/books/{bookId:\\d+}")
@RequiredArgsConstructor
public class BookController {

    private final BooksClient booksClient;

    private final FavouriteBooksClient favouriteBooksClient;

    private final BookReviewsClient reviewsClient;

    @ModelAttribute(name = "book", binding = false)
    public Mono<BookEntity> loadBook(@PathVariable("bookId") int bookId) {
        return this.booksClient.findBook(bookId)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.error(new NoSuchElementException())
                ));
    }

    @ModelAttribute
    public Mono<CsrfToken> loadCsrfToken(ServerWebExchange exchange) {
        return Objects.requireNonNull(exchange.<Mono<CsrfToken>>getAttribute(
                CsrfToken.class.getName())).doOnSuccess(token ->
                exchange.getAttributes()
                        .put(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME, token)
        );
    }

    @GetMapping
    public Mono<String> getBookPage(@ModelAttribute("book") Mono<BookEntity> bookMono, Model model) {
        model.addAttribute("inFavourite", false);
        return bookMono.flatMap(book ->
                this.reviewsClient.findBookReviewsByBookId(book.id())
                        .collectList()
                        .doOnNext(bookReviews -> model.addAttribute("reviews", bookReviews))
                        .then(this.favouriteBooksClient.findFavouriteBookByBookId(book.id()))
                        .doOnNext(favouritesBooks -> model.addAttribute("inFavourite", true))
                        .thenReturn("customer/books/book")
        );
    }

    @PostMapping("add-to-favourites")
    public Mono<String> addBookToFavourites(@ModelAttribute("book") Mono<BookEntity> bookMono) {
        return bookMono
                .map(BookEntity::id)
                .flatMap(bookId -> this.favouriteBooksClient.addBookToFavouriteBooks(bookId)
                        .thenReturn("redirect:/customer/books/%d".formatted(bookId))
                        .onErrorResume(exception -> {
                            log.error(exception.getMessage(), exception);
                            return Mono.just("redirect:/customer/books/%d".formatted(bookId));
                        })
                );
    }

    @PostMapping("remove-from-favourites")
    public Mono<String> removeBookFromFavourites(@ModelAttribute("book") Mono<BookEntity> bookMono) {
        return bookMono
                .map(BookEntity::id)
                .flatMap(bookId -> this.favouriteBooksClient.removeBookFromFavouriteBooks(bookId)
                        .thenReturn("redirect:/customer/books/%d".formatted(bookId)));
    }

    @PostMapping("create-review")
    public Mono<String> createReview(
            @ModelAttribute("book") Mono<BookEntity> bookMono,
            NewBookReviewPayload payload,
            Model model
    ) {
        return bookMono.flatMap(book ->
                this.reviewsClient.createBookReview(book.id(), payload.rating(), payload.review())
                .thenReturn("redirect:/customer/books/%d".formatted(book.id()))
                .onErrorResume(ClientBadRequestException.class, exception -> {
                            model.addAttribute("inFavourite", false);
                            model.addAttribute("payload", payload);
                            model.addAttribute("errors", exception.getErrors());
                            return this.favouriteBooksClient.findFavouriteBookByBookId(book.id())
                                    .doOnNext(favouriteBook ->
                                            model.addAttribute("inFavourite", true))
                                    .thenReturn("customer/books/book");
                        }
                )
        );
    }
}
