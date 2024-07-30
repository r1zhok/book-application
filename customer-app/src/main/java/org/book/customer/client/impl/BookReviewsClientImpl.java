package org.book.customer.client.impl;

import lombok.RequiredArgsConstructor;
import org.book.customer.client.BookReviewsClient;
import org.book.customer.client.exeption.ClientBadRequestException;
import org.book.customer.client.payload.NewBookReviewPayload;
import org.book.customer.entity.BookReviewEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class BookReviewsClientImpl implements BookReviewsClient {

    private final WebClient webClient;

    @Override
    public Flux<BookReviewEntity> findBookReviewsByBookId(Long bookId) {
        return this.webClient.get()
                .uri("feedback-api/book-reviews/by-book-id/{bookId}", bookId)
                .retrieve()
                .bodyToFlux(BookReviewEntity.class);
    }

    @Override
    public Mono<BookReviewEntity> createBookReview(Long bookId, int rating, String review) {
        return this.webClient.post()
                .uri("feedback-api/book-reviews")
                .bodyValue(new NewBookReviewPayload(bookId, rating, review))
                .retrieve()
                .bodyToMono(BookReviewEntity.class)
                .onErrorMap(WebClientResponseException.BadRequest.class
                        , ex -> new ClientBadRequestException(ex,
                                ((List<String>) ex.getResponseBodyAs(ProblemDetail.class)
                                        .getProperties().get("errors"))));
    }
}
