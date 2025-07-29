package io.github.etr.tracting.http.test.dummy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.github.etr.tracting.http.test.dummy.task.ExternalTodoClient;

@SpringBootApplication
public class DummyApp {

    public static void main(String[] args) {
        SpringApplication.run(DummyApp.class, args);
    }

    @Bean
    ExternalTodoClient todoClient(@Value("${todo.api.url}") String baseUrl, RestClient.Builder builder) {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.baseUrl(baseUrl)
                .build()))
            .build()
            .createClient(ExternalTodoClient.class);
    }
}
