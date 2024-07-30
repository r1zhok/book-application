package org.book.feedback.repository;

import org.book.feedback.entity.BookReviewEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BookReviewRepository
        extends ReactiveCrudRepository<BookReviewEntity, UUID> {

    Flux<BookReviewEntity> findAllByBookId(long bookId);
}
