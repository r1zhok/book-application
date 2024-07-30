package org.book.feedback.service;

import org.book.feedback.entity.FavouriteBookEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavouriteBooksService {

    Mono<FavouriteBookEntity> addBookToFavouriteBooks(Long bookId, String userId);

    Mono<Void> removeBookFromFavouriteBooks(Long bookId, String userId);

    Mono<FavouriteBookEntity> findFavouriteBookByBookId(Long bokId, String userId);

    Flux<FavouriteBookEntity> findFavouriteBooks(String userId);
}
