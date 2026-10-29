package com.uo289390.toco.service.recommendation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.InstrumentGenreRepository;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.service.SpotifyAuthService;
import com.uo289390.toco.utils.Identifiable;
import com.uo289390.toco.dto.SongDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.repository.SongRepository.TitleArtist;
import com.uo289390.toco.utils.mapper.DtoMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.uo289390.toco.utils.EntityResolver.resolveUser;
import static com.uo289390.toco.utils.RecommendationConstants.*;
import static com.uo289390.toco.utils.Utils.*;

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

    private static final String SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1";
    private static final String SPOTIFY_TOP_SONGS_ENDPOINT = SPOTIFY_API_BASE_URL + "/me/top/tracks";
    private static final String SPOTIFY_TOP_ARTISTS_ENDPOINT = SPOTIFY_API_BASE_URL + "/me/top/artists";
    private static final int SPOTIFY_MAX_SONGS_LIMIT = 50;

    /**
     * RestTemplate for making HTTP requests to external APIs.
     */
    private final RestTemplate restTemplate;

    /**
     * Provides the user's Spotify access token, refreshing it when it has expired.
     */
    private final SpotifyAuthService spotifyAuthService;

    private final SongRepository songRepository;
    private final LessonRepository lessonRepository;
    private final InstrumentGenreRepository instrumentGenreRepository;

    private final DtoMapper dtoMapper;

    /**
     * Recomienda elementos de todos los tipos a partir de los gustos del usuario en Spotify.
     *
     * 1. Canciones: las de {@link #findSongsForUser} (top del usuario y de sus artistas favoritos).
     * 2. Lecciones: de los géneros de esas canciones (solo las del género, no las de un instrument-genre).
     * 3. Instrument-genres: combinaciones de los géneros e instrumentos más repetidos en esas canciones.
     * 4. Géneros: los más repetidos en esas canciones.
     *
     * Nunca se recomienda nada que el usuario ya tenga en sus listas. El resultado se baraja,
     * ya que la app solo muestra los primeros y en orden de prioridad siempre serían canciones.
     *
     * TODO: Este algoritmo trabaja sobre las canciones que existen en base de datos, pero Spotify devuelve los géneros
     * de los artistas top del usuario, por lo que sería interesante utilizar esa info para las lecciones, géneros, etc,
     * ya que ahora se pierden esos géneros en caso de que no haya canciones del artista en base de datos.
     *
     * @param userId el ID del usuario de la aplicación
     * @return los elementos recomendados, barajados; vacío si ninguna canción de Spotify está en la base de datos
     */
    @Override
    public List<ItemDTO> findItemsForUser(Long userId) {
        log.info("Fetching Spotify recommendations for user: {}", userId);

        // 1. Canciones de Spotify que están en la base de datos (valida el usuario y su token)
        List<Song> songs = findSongEntitiesForUser(userId, N_SPOTIFY_RECOMMENDED_SONGS);
        if (songs.isEmpty()) {
            log.info("No Spotify songs found in the database for user: {}", userId);
            return List.of();
        }

        User user = resolveUser(userId);
        SequencedSet<Item> items = new LinkedHashSet<>(songs);

        // Géneros e instrumentos de las canciones, con repetidos para saber cuáles son los más presentes
        List<Genre> genresInSongs = songs.stream().flatMap(song -> song.getGenres().stream()).toList();
        List<Instrument> instrumentsInSongs = songs.stream().flatMap(song -> song.getInstruments().stream()).toList();

        // Del más al menos repetido (getTopNMostRepeated agrupa por ID, así que no hay duplicados)
        List<Genre> topGenres = getTopNMostRepeated(genresInSongs, genresInSongs.size());

        if (!topGenres.isEmpty()) {
            // 2. Lecciones de los géneros (no de instrument-genre)
            items.addAll(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(
                    idsOf(topGenres),
                    idsToExclude(user.getIdsFromItemListsOfType(Lesson.class)),
                    resolvePageable(N_SPOTIFY_RECOMMENDED_LESSONS)
            ));

            // 3. Combinaciones de los géneros e instrumentos más presentes en las canciones
            List<Instrument> topInstruments = getTopNMostRepeated(instrumentsInSongs, N_TOP_INSTRUMENTS_USED_IN_SPOTIFY_INSTRUMENT_GENRE_RECOMMENDATION);
            if (!topInstruments.isEmpty()) {
                items.addAll(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndIdNotIn(
                        idsOf(topGenres.stream().limit(N_TOP_GENRES_USED_IN_SPOTIFY_INSTRUMENT_GENRE_RECOMMENDATION).toList()),
                        idsOf(topInstruments),
                        idsToExclude(user.getIdsFromItemListsOfType(InstrumentGenre.class)),
                        resolvePageable(N_SPOTIFY_RECOMMENDED_INSTRUMENT_GENRES)
                ));
            }

            // 4. Géneros más presentes que el usuario no tenga ya (ya están cargados, no hace falta consultar)
            Set<Long> genresInLists = user.getIdsFromItemListsOfType(Genre.class);
            topGenres.stream()
                    .filter(genre -> !genresInLists.contains(genre.getId()))
                    .limit(N_SPOTIFY_RECOMMENDED_GENRES)
                    .forEach(items::add);
        }

        log.info("Returning {} Spotify items for user: {}", items.size(), userId);

        // Fusionar, barajar y devolver
        List<Item> shuffled = new ArrayList<>(items);
        Collections.shuffle(shuffled);
        return dtoMapper.itemsToItemDTOs(shuffled);
    }

    @Override
    public List<SongDTO> findSongsForUser(Long userId, int limit) {
        return findSongEntitiesForUser(userId, limit).stream().map(dtoMapper::songToSongDto).toList();
    }

    /**
     * Canciones top del usuario en Spotify que están en la base de datos, completadas con las
     * de sus artistas favoritos si no llegan al límite. Nunca incluye canciones de sus listas.
     *
     * @param userId el ID del usuario de la aplicación
     * @param limit  número máximo de canciones; si no es positivo no se llama a Spotify
     * @return las canciones, en orden de prioridad
     */
    private List<Song> findSongEntitiesForUser(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        HttpEntity<Void> request = authorizedRequest(userId);

        // Nunca se recomiendan canciones que el usuario ya tiene en sus listas
        Set<Long> excludedIds = resolveUser(userId).getIdsFromItemListsOfType(Song.class);

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
        List<Song> found = songRepository.findByTitleAndArtistIn(new LinkedHashSet<>(candidates));
        Map<TitleArtist, Song> songsByKey = found.stream()
                .collect(Collectors.toMap(song -> key(song.getTitle(), song.getArtist()), Function.identity(), (first, second) -> first));

        // 3. Respetar el orden de Spotify, sin repetidos (una canción puede salir en varios periodos), hasta el límite
        List<Song> songs = candidates.stream()
                .map(candidate -> songsByKey.get(key(candidate.title(), candidate.artist())))
                .filter(Objects::nonNull)
                .filter(song -> !excludedIds.contains(song.getId()))
                .distinct()
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));

        log.info("Found {} of {} Spotify top songs in the database for user: {}", songs.size(), candidates.size(), userId);

        int missing = limit - songs.size();
        if (missing <= 0) {
            return songs;
        }

        // Si no están completas aún, sacamos canciones a partir de sus artistas favoritos en medium_term
        List<String> topArtists = fetchTopArtists(request, N_TOP_ARTISTS_USED_IN_SPOTIFY_SONG_RECOMMENDATION, "medium_term");

        // Cada artista aporta la parte entera; el artista top se queda además con el resto,
        // para que entre todos sumen exactamente lo que falta (aunque Spotify devuelva menos artistas)
        int fromOtherArtists = topArtists.isEmpty() ? 0 : missing / topArtists.size();
        int fromTopArtist = missing - fromOtherArtists * (topArtists.size() - 1);

        // Por cada artista: primero sus populares, luego sus clásicas y por último sus nuevas
        List<ArtistSongsQuery> artistQueries = List.of(
                songRepository::findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn,
                songRepository::findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn,
                songRepository::findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn
        );

        // Nunca se repiten canciones: ni las de las listas, ni las del paso anterior, ni las de otro filtro o artista
        songs.forEach(song -> excludedIds.add(song.getId()));

        for (int i = 0; i < topArtists.size(); i++) {
            int missingFromArtist = i == 0 ? fromTopArtist : fromOtherArtists;

            for (ArtistSongsQuery query : artistQueries) {
                if (missingFromArtist <= 0) {
                    break;
                }
                List<Song> fromArtist = query.find(topArtists.get(i), idsToExclude(excludedIds), resolvePageable(missingFromArtist));
                songs.addAll(fromArtist);
                fromArtist.forEach(song -> excludedIds.add(song.getId()));
                missingFromArtist -= fromArtist.size();
            }
        }

        log.info("Returning {} Spotify songs for user: {} ({} from top artists)", songs.size(), userId, songs.size() - (limit - missing));

        return songs;
    }

    /**
     * Valida el usuario y prepara la petición a Spotify con su token, que se renueva
     * (y se guarda) automáticamente si ha caducado.
     *
     * @param userId el ID del usuario de la aplicación
     * @return petición con la cabecera de autorización del usuario
     */
    private HttpEntity<Void> authorizedRequest(Long userId) {
        if (userId == null) {
            log.error("Invalid Spotify user ID: null");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(spotifyAuthService.getValidAccessToken(userId));
        return new HttpEntity<>(headers);
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
        return fetchTopItems(request, SPOTIFY_TOP_SONGS_ENDPOINT, count, timeRange).stream()
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
     * Pide a Spotify los artistas top del usuario en un periodo y se queda solo con su nombre.
     *
     * @param request   petición con la cabecera de autorización del usuario
     * @param count     número de artistas a pedir; si es 0 no se llama a Spotify
     * @param timeRange periodo de Spotify: {@code short_term}, {@code medium_term} o {@code long_term}
     * @return los nombres de los artistas, en el orden de Spotify
     */
    private List<String> fetchTopArtists(HttpEntity<Void> request, int count, String timeRange) {
        return fetchTopItems(request, SPOTIFY_TOP_ARTISTS_ENDPOINT, count, timeRange).stream()
                .map(artist -> (String) artist.get("name"))
                .filter(Objects::nonNull)
                .map(String::trim)
                .toList();
    }

    /**
     * Llama a un endpoint "top" de Spotify ({@code /me/top/tracks} o {@code /me/top/artists})
     * y devuelve sus {@code items} sin transformar.
     *
     * @param request   petición con la cabecera de autorización del usuario
     * @param endpoint  URL del endpoint
     * @param count     número de elementos a pedir; si es 0 no se llama a Spotify
     * @param timeRange periodo de Spotify: {@code short_term}, {@code medium_term} o {@code long_term}
     * @return los elementos devueltos, en el orden de Spotify; vacío si no hay ninguno
     */
    private List<Map<String, Object>> fetchTopItems(HttpEntity<Void> request, String endpoint, int count, String timeRange) {
        if (count <= 0) {
            return List.of();
        }

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                endpoint + "?limit=" + count + "&time_range=" + timeRange,
                HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        return Optional.ofNullable(response.getBody())
                .map(body -> (List<Map<String, Object>>) body.get("items"))
                .orElseGet(List::of);
    }

    /**
     * Consulta de canciones de un artista con uno de sus filtros (popular, clásica o nueva).
     */
    @FunctionalInterface
    private interface ArtistSongsQuery {
        List<Song> find(String artist, Collection<Long> excludedIds, Pageable pageable);
    }

    /**
     * IDs de los elementos dados, en el mismo orden.
     */
    private static List<Long> idsOf(Collection<? extends Identifiable<Long>> items) {
        return items.stream().map(Identifiable::getId).toList();
    }

    /**
     * Clave para emparejar canciones de Spotify con las de la base de datos (sin distinguir mayúsculas).
     */
    private static TitleArtist key(String title, String artist) {
        return new TitleArtist(title.trim().toLowerCase(), artist.trim().toLowerCase());
    }
}
