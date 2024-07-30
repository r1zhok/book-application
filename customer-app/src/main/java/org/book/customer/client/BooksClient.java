package org.book.customer.client;

import org.book.customer.entity.BookEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BooksClient {

    Flux<BookEntity> findAllBooks(String filter);

    Mono<BookEntity> findBook(int bookId);
}
