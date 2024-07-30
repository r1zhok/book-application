package org.book.customer.client;

import org.book.customer.entity.FavouriteBookEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavouriteBooksClient {

    Mono<FavouriteBookEntity> findFavouriteBookByBookId(Long bookId);

    Mono<FavouriteBookEntity> addBookToFavouriteBooks(Long bookId);

    Mono<Void> removeBookFromFavouriteBooks(Long bookId);

    Flux<FavouriteBookEntity> findFavouriteBooks();
}
