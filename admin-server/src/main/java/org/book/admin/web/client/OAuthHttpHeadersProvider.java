package org.book.admin.web.client;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

@RequiredArgsConstructor
public class OAuthHttpHeadersProvider implements HttpHeadersProvider {

    private final OAuth2AuthorizedClientManager clientManager;

    @Override
    public HttpHeaders getHeaders(Instance instance) {
        OAuth2AuthorizedClient oAuth2AuthorizedClient = this.clientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                        .principal("admin-service")
                        .build()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(oAuth2AuthorizedClient.getAccessToken().getTokenValue());
        return headers;
    }
}
