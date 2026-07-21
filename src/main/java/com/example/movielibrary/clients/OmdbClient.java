package com.example.movielibrary.clients;

import com.example.movielibrary.models.omdb.OmdbResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Year;

@Component
public class OmdbClient {
    private final RestClient restClient;
    private final String apiKey;

    public OmdbClient(RestClient.Builder restClientBuilder,
                      @Value("${omdb.api.key}") String apiKey,
                      @Value("${omdb.api.base-url}") String url
    ) {
        this.restClient = restClientBuilder.baseUrl(url).build();
        this.apiKey = apiKey;
    }

    public OmdbResponseDto findMovie(String title, Year year) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .queryParam("apiKey", apiKey)
                            .queryParam("t", title)
                            .queryParam("type", "movie");

                    if (year != null) {
                        uriBuilder.queryParam("y", year.getValue());
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(OmdbResponseDto.class);
    }
}
