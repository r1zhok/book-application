package org.book.customer.client.impl;

import lombok.RequiredArgsConstructor;
import org.book.customer.client.FavouriteBooksClient;
import org.book.customer.client.exeption.ClientBadRequestException;
import org.book.customer.client.payload.NewFavouriteBookPayload;
import org.book.customer.entity.FavouriteBookEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class FavouriteBooksClientImpl implements FavouriteBooksClient {

    private final WebClient webClient;

    @Override
    public Mono<FavouriteBookEntity> findFavouriteBookByBookId(Long bookId) {
        return this.webClient.get()
                .uri("/feedback-api/favourite-books/by-book-id/{bookId}", bookId)
                .retrieve()
                .bodyToMono(FavouriteBookEntity.class)
                .onErrorComplete(WebClientResponseException.NotFound.class);
    }

    @Override
    public Mono<FavouriteBookEntity> addBookToFavouriteBooks(Long bookId) {
        return this.webClient.post()
                .uri("/feedback-api/favourite-books")
                .bodyValue(new NewFavouriteBookPayload(bookId))
                .retrieve()
                .bodyToMono(FavouriteBookEntity.class)
                .onErrorMap(WebClientResponseException.BadRequest.class
                        , ex -> new ClientBadRequestException(ex,
                                ((List<String>) ex.getResponseBodyAs(ProblemDetail.class)
                                        .getProperties().get("errors"))));
    }

    @Override
    public Mono<Void> removeBookFromFavouriteBooks(Long bookId) {
        return this.webClient.delete()
                .uri("/feedback-api/favourite-books/by-book-id/{bookId}", bookId)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    @Override
    public Flux<FavouriteBookEntity> findFavouriteBooks() {
        return this.webClient.get()
                .uri("/feedback-api/favourite-books")
                .retrieve()
                .bodyToFlux(FavouriteBookEntity.class);
    }
}
