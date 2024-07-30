package org.book.feedback.repository;

import org.book.feedback.entity.FavouriteBookEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FavouriteBooksRepository
        extends ReactiveCrudRepository<FavouriteBookEntity, UUID> {

    Flux<FavouriteBookEntity> findAllByUserId(String userId);

    Mono<Void> deleteByBookIdAndUserId(Long bookId, String userId);

    Mono<FavouriteBookEntity> findByBookIdAndUserId(Long bokId, String userId);
}
