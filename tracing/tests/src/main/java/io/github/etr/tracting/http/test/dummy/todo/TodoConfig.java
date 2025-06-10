package io.github.etr.tracting.http.test.dummy.todo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class TodoConfig {

    @Bean
    TodoClient todoClient(@Value("${todo.api.url}") String baseUrl, RestClient.Builder builder) {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.baseUrl(baseUrl)
                .build()))
            .build()
            .createClient(TodoClient.class);
    }
}
