package org.book.feedback.service;

import org.book.feedback.entity.BookReviewEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookReviewsService {

    Mono<BookReviewEntity> createBookReview(Long bookId, int rating, String review, String userId);

    Flux<BookReviewEntity> findBookReviewsByBookId(Long bookId);
}
