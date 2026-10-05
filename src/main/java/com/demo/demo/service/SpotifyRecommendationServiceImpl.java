package com.demo.demo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.demo.demo.dto.ItemDTO;
import jakarta.persistence.criteria.Predicate;
import com.demo.demo.dto.SongDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.demo.demo.repository.SongRepository;
import com.demo.demo.model.Song;
import com.demo.demo.utils.mapper.DtoMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link SpotifyRecommendationService}.
 *
 * Retrieves personalized music recommendations from the Spotify API based on
 * the user's top artists.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotifyRecommendationServiceImpl implements SpotifyRecommendationService {

    private final String SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1";
    // Spotify llama "tracks" a las canciones: /me/top/songs no existe
    private final String SPOTIFY_TOP_SONGS_ENDPOINT = SPOTIFY_API_BASE_URL + "/me/top/tracks";

    private final int SPOTIFY_MAX_SONGS_LIMIT = 50;

    /**
     * RestTemplate for making HTTP requests to external APIs.
     */
    private final RestTemplate restTemplate;

    /**
     * Provides the user's Spotify access token, refreshing it when it has expired.
     */
    private final SpotifyAuthService spotifyAuthService;

    private final SongRepository songRepository;

    private final DtoMapper dtoMapper;

    @Override
    public List<ItemDTO> findItemsForUser(Long userId) {
        if (userId == null) {
            log.error("Invalid Spotify user ID: {}", userId);
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        log.info("Fetching Spotify recommendations for user: {}", userId);

        // Token válido: si ha caducado se renueva (y se guarda) automáticamente
        String accessToken = spotifyAuthService.getValidAccessToken(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // 1. Obtener artistas top del usuario
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.spotify.com/v1/me/top/artists?limit=2",
                HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        List<Map<String, Object>> artists = Optional.ofNullable(response.getBody())
                .map(body -> (List<Map<String, Object>>) body.get("items"))
                .filter(items -> !items.isEmpty())
                .orElseThrow(() -> {
                    log.warn("No se encontraron artistas favoritos para el usuario con ID: {}", userId);
                    return new IllegalStateException("No se encontraron artistas del usuario");
                });

        String seedArtist = Optional.ofNullable((String) artists.get(0).get("id"))
                .filter(id -> !id.isEmpty())
                .orElseThrow(() -> {
                    log.error("El ID del artista semilla está vacío o es nulo para el usuario con ID: {}", userId);
                    return new IllegalStateException("El ID del artista semilla no es válido");
                });

        // 2. Pedir recomendaciones
        String url = "https://api.spotify.com/v1/artists/" + seedArtist + "/top-tracks";
        log.info("Seed artist: {}", seedArtist);
        log.info("Requesting recommendations from Spotify API: {}", url);

        try {
            ResponseEntity<Map<String, Object>> recResponse = restTemplate.exchange(
                    url, HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (!recResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Spotify API returned non-successful status: {} with body: {}",
                        recResponse.getStatusCode(), recResponse.getBody());
                throw new IllegalStateException("Error fetching recommendations from Spotify API");
            }

            List<Map<String, Object>> recommendations = Optional.ofNullable(recResponse.getBody())
                    .map(body -> (List<Map<String, Object>>) body.get("tracks"))
                    .orElseGet(List::of);

            log.info("Successfully fetched {} recommendations for user: {}",
                    recommendations.size(), userId);

            return null;
        } catch (Exception e) {
            log.error("Error fetching recommendations from Spotify API: {}", e.getMessage(), e);
            throw new IllegalStateException("Error fetching recommendations from Spotify API", e);
        }
    }

    @Override
    public List<SongDTO> findSongsForUser(Long userId, int limit) {
        if (userId == null) {
            log.error("Invalid Spotify user ID: {}", userId);
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        log.info("Fetching Spotify recommendations for user: {}", userId);

        // Token válido: si ha caducado se renueva (y se guarda) automáticamente
        String accessToken = spotifyAuthService.getValidAccessToken(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        if (limit <= 0) {
            return List.of();
        }

        // Pedir el doble de canciones por si algunas no están disponibles en la base de datos, limitando a un máximo de 50
        int requested = Math.min(limit * 2, SPOTIFY_MAX_SONGS_LIMIT);

        // Medio plazo: la mitad; largo y corto plazo: la otra mitad a partes iguales
        int midTerm = requested / 2;
        int shortTerm = (requested - midTerm) / 2;
        int longTerm = requested - midTerm - shortTerm;

        // 1. Canciones top del usuario en los tres periodos, en orden de prioridad (solo título y artista)
        List<TitleArtist> candidates = new ArrayList<>();
        candidates.addAll(fetchTopTracks(request, midTerm, "medium_term"));
        candidates.addAll(fetchTopTracks(request, longTerm, "long_term"));
        candidates.addAll(fetchTopTracks(request, shortTerm, "short_term"));

        // 2. Buscarlas todas en la base de datos con una sola consulta
        List<Song> found = findByTitleAndArtistIn(new LinkedHashSet<>(candidates));
        Map<TitleArtist, Song> songsByKey = found.stream()
                .collect(Collectors.toMap(song -> key(song.getTitle(), song.getArtist()), Function.identity(), (first, second) -> first));

        // 3. Respetar el orden de Spotify, sin repetidos (una canción puede salir en varios periodos), hasta el límite
        List<SongDTO> songs = candidates.stream()
                .map(candidate -> songsByKey.get(key(candidate.title(), candidate.artist())))
                .filter(Objects::nonNull)
                .distinct()
                .limit(limit)
                .map(dtoMapper::songToSongDto)
                .toList();

        log.info("Found {} of {} Spotify top songs in the database for user: {}", songs.size(), candidates.size(), userId);

        // @todo Siguiente nivel: si no se llega al límite (canciones repetidas o que no están en la
        //       base de datos), completar con otras recomendaciones.
        return songs;
    }

    /**
     * Pide a Spotify las canciones top del usuario en un periodo y se queda solo con título y artista.
     *
     * @param request   petición con la cabecera de autorización del usuario
     * @param count     número de canciones a pedir; si es 0 no se llama a Spotify
     * @param timeRange periodo de Spotify: {@code short_term}, {@code medium_term} o {@code long_term}
     * @return las canciones (título y artista principal), en el orden de Spotify
     */
    private List<TitleArtist> fetchTopTracks(HttpEntity<Void> request, int count, String timeRange) {
        if (count <= 0) {
            return List.of();
        }

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                SPOTIFY_TOP_SONGS_ENDPOINT + "?limit=" + count + "&time_range=" + timeRange,
                HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        List<Map<String, Object>> tracks = Optional.ofNullable(response.getBody())
                .map(body -> (List<Map<String, Object>>) body.get("items"))
                .orElseGet(List::of);

        return tracks.stream()
                .map(track -> {
                    String title = (String) track.get("name");
                    String artist = Optional.ofNullable((List<Map<String, Object>>) track.get("artists"))
                            .filter(artists -> !artists.isEmpty())
                            .map(artists -> (String) artists.getFirst().get("name"))
                            .orElse(null);
                    return title == null || artist == null ? null : new TitleArtist(title.trim(), artist.trim());
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Título y artista de una canción tal como la identifica Spotify.
     */
    private record TitleArtist(String title, String artist) {}

    /**
     * Busca en una sola consulta las canciones que coinciden con alguna de las parejas
     * (título, artista), sin distinguir mayúsculas.
     *
     * @param pairs parejas a buscar
     * @return las canciones encontradas, sin orden; vacío si no hay parejas (no se lanza la consulta)
     */
    private List<Song> findByTitleAndArtistIn(Collection<TitleArtist> pairs) {
        if (pairs.isEmpty()) {
            return List.of();
        }
        return songRepository.findAll((root, query, cb) -> cb.or(pairs.stream()
                .map(pair -> cb.and(
                        cb.equal(cb.lower(root.get("title")), pair.title().toLowerCase()),
                        cb.equal(cb.lower(root.get("artist")), pair.artist().toLowerCase())))
                .toArray(Predicate[]::new)));
    }

    /**
     * Clave para emparejar canciones de Spotify con las de la base de datos (sin distinguir mayúsculas).
     */
    private static TitleArtist key(String title, String artist) {
        return new TitleArtist(title.trim().toLowerCase(), artist.trim().toLowerCase());
    }
}
