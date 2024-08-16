package org.book.catalogue.service;

import org.book.catalogue.entity.BookEntity;
import org.book.catalogue.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BooksService {

    private final BookRepository repository;

    public Iterable<BookEntity> findAllBooks(String filter) {
        if (filter != null && !filter.isBlank()) {
            return this.repository.findBookEntitiesByAuthor(filter);
        } else {
            return this.repository.findAll();
        }
    }

    @Transactional
    public BookEntity createBook(String name, String author, String details) {
        return repository.save(new BookEntity(null, name, author, details));
    }

    public Optional<BookEntity> findBook(Long bookId) {
        return this.repository.findById(bookId);
    }

    @Transactional
    public void updateBook(Long id,String name, String author, String details) {
        this.repository.findById(id)
                .ifPresentOrElse(product -> {
                    product.setName(name);
                    product.setAuthor(author);
                    product.setDetails(details);
                }, () -> {
                    throw new NoSuchElementException();
                });
    }

    @Transactional
    public void deleteProduct(Long id) {
        this.repository.findById(id);
    }
}
