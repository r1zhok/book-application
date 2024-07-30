package org.book.feedback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.book.feedback.controller.payload.NewBookReviewPayload;
import org.book.feedback.entity.BookReviewEntity;
import org.book.feedback.service.BookReviewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/book-reviews")
public class BookReviewsRestController {

    private final BookReviewsService service;

    @GetMapping("by-book-id/{bookId}")
    public Flux<BookReviewEntity> findBookReviewsByBookId(@PathVariable("bookId") Long bookId) {
        return this.service.findBookReviewsByBookId(bookId);
    }

    @PostMapping
    public Mono<ResponseEntity<BookReviewEntity>> createBookReview(
            @Valid @RequestBody Mono<NewBookReviewPayload> payloadMono,
            Mono<JwtAuthenticationToken> authenticationTokenMono,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        return authenticationTokenMono.flatMap(token -> payloadMono.flatMap(payload ->
                        this.service.createBookReview(payload.bookId(), payload.rating(),
                                payload.review(), token.getToken().getSubject()))
                .map(bookReview -> ResponseEntity.created(
                        uriComponentsBuilder.replacePath("/feedback-api/book-reviews/{id}").build(bookReview.getId())
                ).body(bookReview)));
    }
}
