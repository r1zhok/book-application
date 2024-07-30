package org.book.customer.config;

import org.book.customer.client.impl.BookReviewsClientImpl;
import org.book.customer.client.impl.BooksClientImpl;
import org.book.customer.client.impl.FavouriteBooksClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    @Scope("prototype")
    public WebClient.Builder bookServicesWebClientBuilder(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
                ServerOAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        var filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                clientRegistrationRepository, authorizedClientRepository);
        filter.setDefaultClientRegistrationId("keycloak");
        return WebClient.builder()
                .filter(filter);
    }

    @Bean
    public BooksClientImpl booksClient(
            @Value("${books.services.catalogue.uri:http://localhost:8081}") String catalogueUri,
            WebClient.Builder bookServicesWebClientBuilder
    ) {
        return new BooksClientImpl(bookServicesWebClientBuilder
                .baseUrl(catalogueUri)
                .build()
        );
    }

    @Bean
    public FavouriteBooksClientImpl favouriteBooksClient(
            @Value("${books.services.feedback.uri:http://localhost:8084}") String feedbackUri,
            WebClient.Builder bookServicesWebClientBuilder
    ) {
        return new FavouriteBooksClientImpl(bookServicesWebClientBuilder
                .baseUrl(feedbackUri)
                .build()
        );
    }

    @Bean
    public BookReviewsClientImpl bookReviewsClient(
            @Value("${books.services.feedback.uri:http://localhost:8084}") String feedbackUri,
            WebClient.Builder bookServicesWebClientBuilder
    ) {
        return new BookReviewsClientImpl(bookServicesWebClientBuilder
                .baseUrl(feedbackUri)
                .build()
        );
    }
}
