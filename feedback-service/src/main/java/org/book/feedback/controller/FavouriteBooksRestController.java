package org.book.feedback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.book.feedback.controller.payload.NewFavouriteBookPayload;
import org.book.feedback.entity.FavouriteBookEntity;
import org.book.feedback.service.FavouriteBooksService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/favourite-books")
public class FavouriteBooksRestController {

    private final FavouriteBooksService service;

    @GetMapping
    public Flux<FavouriteBookEntity> findFavouriteBooks(Mono<JwtAuthenticationToken> authenticationTokenMono) {
        return authenticationTokenMono.flatMapMany(token ->
                this.service.findFavouriteBooks(token.getToken().getSubject()));
    }

    @GetMapping("by-book-id/{bookId:\\d+}")
    public Mono<FavouriteBookEntity> findFavouriteBookByBookId(@PathVariable("bookId") Long bookId,
                                                               Mono<JwtAuthenticationToken> authenticationTokenMono) {
        return authenticationTokenMono.flatMap(token ->
                this.service.findFavouriteBookByBookId(bookId, token.getToken().getSubject()));
    }

    @PostMapping
    public Mono<ResponseEntity<FavouriteBookEntity>> addBookToFavouriteBooks(
            @Valid @RequestBody Mono<NewFavouriteBookPayload> payloadMono,
            Mono<JwtAuthenticationToken> authenticationTokenMono,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        return Mono.zip(authenticationTokenMono, payloadMono)
                .flatMap(tuple ->
                                this.service.addBookToFavouriteBooks(tuple.getT2().bookId(),
                                        tuple.getT1().getToken().getSubject()))
                        .map(favouriteBook -> ResponseEntity.created(
                                uriComponentsBuilder.replacePath("/feedback-api/favourite-books/{id}")
                                        .build(favouriteBook.getId())
                        ).body(favouriteBook));
    }

    @DeleteMapping("by-book-id/{bookId:\\d+}")
    public Mono<ResponseEntity<Void>> removeBookFromFavouriteBooks(@PathVariable("bookId") Long bookId,
                                                                   Mono<JwtAuthenticationToken> authenticationTokenMono) {
        return authenticationTokenMono.flatMap(token ->
                this.service.removeBookFromFavouriteBooks(bookId, token.getToken().getSubject())
                        .then(Mono.just(ResponseEntity.noContent().build())));
    }
}
