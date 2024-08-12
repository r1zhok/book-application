package org.book.manager.client;

import org.book.manager.entity.BookEntity;

import java.util.List;
import java.util.Optional;

public interface BooksRestClient {

    List<BookEntity> findAllBooks(String filter);

    BookEntity createBook(String name, String author, String details);

    Optional<BookEntity> findBook(Long bookId);

    void updateBook(Long bookId, String name, String author, String details);

    void deleteBook(Long bookId);
}
