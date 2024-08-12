package org.book.manager.client;

import org.book.manager.controller.payload.NewBookPayload;
import org.book.manager.entity.BookEntity;
import org.book.manager.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@RequiredArgsConstructor
public class BooksRestClientImpl implements BooksRestClient {

    private static final ParameterizedTypeReference<List<BookEntity>> BOOKS_TYPE_REFERENCE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    private static final String BASIC_URI = "/catalogue-api/books";

    @Override
    public List<BookEntity> findAllBooks(String filter) {
        return this.restClient
                .get()
                .uri("/catalogue-api/books?filter={filter}", filter)
                .retrieve()
                .body(BOOKS_TYPE_REFERENCE);
    }

    @Override
    public BookEntity createBook(String name, String author, String details) {
        try {
            return this.restClient
                    .post()
                    .uri(BASIC_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NewBookPayload(name, author, details))
                    .retrieve()
                    .body(BookEntity.class);
        } catch (HttpClientErrorException.BadRequest exception) {
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    @Override
    public Optional<BookEntity> findBook(Long bookId) {
        try {
            return Optional.ofNullable(this.restClient
                    .get()
                    .uri("/catalogue-api/books/{bookId}", bookId)
                    .retrieve()
                    .body(BookEntity.class));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }

    @Override
    public void updateBook(Long bookId, String name, String author, String details) {
        try {
            this.restClient
                    .patch()
                    .uri("/catalogue-api/books/{bookId}", bookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NewBookPayload(name, author, details))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound exception) {
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    @Override
    public void deleteBook(Long bookId) {
        try {
            this.restClient
                    .delete()
                    .uri("/catalogue-api/books/{bookId}", bookId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new NoSuchElementException(exception);
        }
    }
}
