package org.book.customer.client.impl;

import lombok.RequiredArgsConstructor;
import org.book.customer.client.BooksClient;
import org.book.customer.entity.BookEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BooksClientImpl implements BooksClient {

    private final WebClient webClient;

    @Override
    public Flux<BookEntity> findAllBooks(String filter) {
        return this.webClient.get()
                .uri("/catalogue-api/books?filter={filter}", filter)
                .retrieve()
                .bodyToFlux(BookEntity.class);
    }

    @Override
    public Mono<BookEntity> findBook(int bookId) {
        return this.webClient.get()
                .uri("/catalogue-api/books/{bookId}", bookId)
                .retrieve()
                .bodyToMono(BookEntity.class)
                .onErrorComplete(WebClientResponseException.NotFound.class);
    }
}
