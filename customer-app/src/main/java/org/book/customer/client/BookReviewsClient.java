package org.book.customer.client;

import org.book.customer.entity.BookReviewEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookReviewsClient {

    Flux<BookReviewEntity> findBookReviewsByBookId(Long bookId);

    Mono<BookReviewEntity> createBookReview(Long bookId, int rating, String review);
}
