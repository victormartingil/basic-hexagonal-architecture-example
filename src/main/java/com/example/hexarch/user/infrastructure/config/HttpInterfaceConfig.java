package com.example.hexarch.user.infrastructure.config;

import com.example.hexarch.user.infrastructure.http.client.ExternalUserApiHttpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuration for HTTP Interface declarative clients (Spring Framework 6+).
 * Creates dynamic proxies using {@link RestClient} as HTTP backend.
 *
 * @see ExternalUserApiHttpInterface
 * @see RestClientConfig#jsonPlaceholderRestClient()
 * @see docs/18-HTTP-Clients-Comparison-Guide.md
 */
@Configuration
public class HttpInterfaceConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpInterfaceConfig.class);

    @Bean
    public ExternalUserApiHttpInterface jsonPlaceholderHttpInterface(
            @Qualifier("jsonPlaceholderRestClient") RestClient restClient) {

        logger.info("Creating HTTP Interface proxy for ExternalUserApiHttpInterface");

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        ExternalUserApiHttpInterface httpInterface = factory.createClient(ExternalUserApiHttpInterface.class);

        logger.info("HTTP Interface proxy created successfully");

        return httpInterface;
    }
}
