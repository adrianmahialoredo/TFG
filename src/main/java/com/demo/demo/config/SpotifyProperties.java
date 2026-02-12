package com.demo.demo.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;


@Getter
@ConfigurationProperties(prefix = "spotify")
public class SpotifyProperties {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    @ConstructorBinding
    public SpotifyProperties(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

}
