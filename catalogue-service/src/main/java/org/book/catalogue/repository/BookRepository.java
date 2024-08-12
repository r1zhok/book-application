package org.book.catalogue.repository;

import org.book.catalogue.entity.BookEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends CrudRepository<BookEntity, Long> {

    Iterable<BookEntity> findBookEntitiesByAuthor(String filter);
}