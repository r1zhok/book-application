package org.book.feedback.service.impl;

import lombok.RequiredArgsConstructor;
import org.book.feedback.entity.FavouriteBookEntity;
import org.book.feedback.repository.FavouriteBooksRepository;
import org.book.feedback.service.FavouriteBooksService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavouriteBooksServiceImpl implements FavouriteBooksService {

    private final FavouriteBooksRepository repository;

    @Override
    public Mono<FavouriteBookEntity> addBookToFavouriteBooks(Long bookId, String userId) {
        return this.repository.save(new FavouriteBookEntity(UUID.randomUUID(), bookId, userId));
    }

    @Override
    public Mono<Void> removeBookFromFavouriteBooks(Long bookId, String userId) {
        return this.repository.deleteByBookIdAndUserId(bookId, userId);
    }

    @Override
    public Mono<FavouriteBookEntity> findFavouriteBookByBookId(Long bokId, String userId) {
        return repository.findByBookIdAndUserId(bokId, userId);
    }

    @Override
    public Flux<FavouriteBookEntity> findFavouriteBooks(String userId) {
        return this.repository.findAllByUserId(userId);
    }
}
