package org.book.customer.config;

import org.book.customer.client.impl.BookReviewsClientImpl;
import org.book.customer.client.impl.BooksClientImpl;
import org.book.customer.client.impl.FavouriteBooksClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public ReactiveClientRegistrationRepository mockClientRegistrationRepository() {
        return mock(ReactiveClientRegistrationRepository.class);
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository mockAuthorizedClientRepository() {
        return mock(ServerOAuth2AuthorizedClientRepository.class);
    }

    @Bean
    @Primary
    public BooksClientImpl mockBooksClient() {
        return new BooksClientImpl(WebClient.builder()
                .baseUrl("http://localhost:54321")
                .build()
        );
    }

    @Bean
    @Primary
    public FavouriteBooksClientImpl mockFavouriteBooksClient() {
        return new FavouriteBooksClientImpl(WebClient.builder()
                .baseUrl("http://localhost:54321")
                .build()
        );
    }

    @Bean
    @Primary
    public BookReviewsClientImpl mockBookReviewsClient() {
        return new BookReviewsClientImpl(WebClient.builder()
                .baseUrl("http://localhost:54321")
                .build()
        );
    }
}
