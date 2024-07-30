package org.book.feedback.service.impl;

import lombok.RequiredArgsConstructor;
import org.book.feedback.entity.BookReviewEntity;
import org.book.feedback.repository.BookReviewRepository;
import org.book.feedback.service.BookReviewsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookReviewsServiceImpl implements BookReviewsService {

    private final BookReviewRepository repository;

    @Override
    public Mono<BookReviewEntity> createBookReview(Long bookId, int rating, String review, String userId) {
        return repository.save(
                new BookReviewEntity(UUID.randomUUID(), bookId, rating, review, userId)
        );
    }

    @Override
    public Flux<BookReviewEntity> findBookReviewsByBookId(Long bookId) {
        return this.repository.findAllByBookId(bookId);
    }
}
