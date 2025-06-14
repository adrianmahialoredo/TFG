package com.demo.demo.model;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "spotify_users")
public class SpotifyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String spotifyUserId;

    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiration;
    
}
