package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.uo289390.toco.dto.LoginResponse;
import com.uo289390.toco.dto.UserDto;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.utils.EntityResolver;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link UserServiceImpl}.
 * Tests registration (uniqueness checks, password hashing), login, CRUD operations
 * and favourite genres/instruments management.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Spy
    private DtoMapper dtoMapper = new DtoMapper();

    @Spy
    private EntityMapper entityMapper = new EntityMapper();

    private UserServiceImpl userService;

    private User user;
    private Genre rock;
    private Genre jazz;
    private Genre pop;
    private Instrument guitar;
    private Instrument piano;
    private Instrument drums;

    @BeforeEach
    void setUp() {
        new EntityResolver(mock(ChordRepository.class), genreRepository, instrumentRepository,
                mock(InstrumentGenreRepository.class), mock(ItemRepository.class), mock(ItemListRepository.class),
                mock(LessonRepository.class), mock(LessonSectionRepository.class), mock(SongRepository.class),
                mock(SongLessonRepository.class), mock(SpotifyUserRepository.class), mock(TabSectionRepository.class),
                mock(TuneRepository.class),
                userRepository);
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtUtil, dtoMapper, entityMapper);

        rock = genre(1L, "Rock");
        jazz = genre(2L, "Jazz");
        pop = genre(3L, "Pop");
        guitar = instrument(10L, "Guitar");
        piano = instrument(11L, "Piano");
        drums = instrument(12L, "Drums");

        user = new User();
        user.setId(100L);
        user.setEmail("john@example.com");
        user.setUsername("john");
        user.setName("John Doe");
        user.setPassword("$2a$hashed");
        user.setProfileImageUrl("https://example.com/john.png");
        user.getPreferences().setFavoriteGenres(new LinkedHashSet<>(List.of(rock, jazz)));
        user.getPreferences().setFavoriteInstruments(new LinkedHashSet<>(List.of(guitar, piano)));
    }

    private static Genre genre(Long id, String title) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle(title);
        return genre;
    }

    private static Instrument instrument(Long id, String name) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName(name);
        return instrument;
    }

    private UserDto userDto() {
        UserDto dto = new UserDto();
        dto.setEmail("jane@example.com");
        dto.setUsername("jane");
        dto.setName("Jane Doe");
        dto.setPassword("Secret1!");
        dto.setProfileImageUrl("https://example.com/jane.png");
        dto.setFavouriteGenresIds(List.of(1L, 2L));
        dto.setFavouriteInstrumentsIds(List.of(10L, 11L));
        return dto;
    }

    private void stubFavouritesResolution() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(rock));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(jazz));
        when(instrumentRepository.findById(10L)).thenReturn(Optional.of(guitar));
        when(instrumentRepository.findById(11L)).thenReturn(Optional.of(piano));
    }

    private User captureSaved() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        return captor.getValue();
    }

    // -------- Register --------

    @Test
    void testRegister_Success() {
        // Arrange
        UserDto dto = userDto();
        when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.empty());
        stubFavouritesResolution();
        when(passwordEncoder.encode("Secret1!")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        // Act
        UserDto result = userService.register(dto);

        // Assert
        User saved = captureSaved();
        assertEquals("jane@example.com", saved.getEmail());
        assertEquals("jane", saved.getUsername());
        assertEquals("Jane Doe", saved.getName());
        assertEquals("$2a$encoded", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());
        assertEquals(Set.of(rock, jazz), saved.getPreferences().getFavoriteGenres());
        assertEquals(Set.of(guitar, piano), saved.getPreferences().getFavoriteInstruments());

        assertEquals(200L, result.getId());
        assertEquals("jane@example.com", result.getEmail());
        assertNull(result.getPassword());
        assertEquals(List.of(1L, 2L), result.getFavouriteGenresIds());
        assertEquals(List.of(10L, 11L), result.getFavouriteInstrumentsIds());
        assertFalse(result.isSpotifyConnected());
    }

    @Test
    void testRegister_PasswordIsNeverStoredInPlainText() {
        // Arrange
        BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(userRepository, realEncoder, jwtUtil, dtoMapper, entityMapper);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        stubFavouritesResolution();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.register(userDto());

        // Assert
        String stored = captureSaved().getPassword();
        assertNotEquals("Secret1!", stored);
        assertTrue(realEncoder.matches("Secret1!", stored));
    }

    @Test
    void testRegister_DuplicateEmailThrows() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.register(userDto()));
        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testRegister_DuplicateEmailWithOtherCaseThrows() {
        // Arrange
        UserDto dto = userDto();
        dto.setEmail(" Jane@Example.COM ");
        when(userRepository.existsByEmailIgnoreCase("Jane@Example.COM")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.register(dto));
        assertEquals("Email already registered", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_DuplicateUsernameThrows() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.register(userDto()));
        assertEquals("Username already taken", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_UnknownGenreThrows() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.register(userDto()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_UnknownInstrumentThrows() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(genreRepository.findById(1L)).thenReturn(Optional.of(rock));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(jazz));
        when(instrumentRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.register(userDto()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_RepositoryFailureIsRethrown() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        stubFavouritesResolution();
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("db down"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(userDto()));
        assertEquals("db down", ex.getMessage());
    }

    // -------- Password reset --------

    @Test
    void resetPassword_encodesAndSavesTheNewPassword() {
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecret1!")).thenReturn("$2a$new");

        userService.resetPassword("  john@example.com ", "NewSecret1!");

        assertEquals("$2a$new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_unknownEmail_throwsIllegalArgumentWithoutSaving() {
        when(userRepository.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword("nobody@example.com", "NewSecret1!"));

        assertEquals("Email not registered", ex.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    // -------- Login --------

    @Test
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret1!", "$2a$hashed")).thenReturn(true);
        when(jwtUtil.generateToken(100L, Role.USER)).thenReturn("jwt-token");

        // Act
        LoginResponse response = userService.login("john@example.com", "Secret1!");

        // Assert
        assertEquals("jwt-token", response.getToken());
        assertEquals(100L, response.getUser().getId());
        assertEquals("john@example.com", response.getUser().getEmail());
        assertEquals("john", response.getUser().getUsername());
        assertEquals(Set.of(1L, 2L), Set.copyOf(response.getUser().getFavouriteGenresIds()));
        verify(jwtUtil).generateToken(100L, Role.USER);
    }

    @Test
    void testLogin_UnknownEmailOrUsernameThrowsBadCredentials() {
        // Arrange
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("nobody@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> userService.login("nobody@example.com", "x"));
        assertEquals("Invalid credentials", ex.getMessage());
        verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void testLogin_WithUsername() {
        // Arrange
        when(userRepository.findByEmail("john")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret1!", "$2a$hashed")).thenReturn(true);
        when(jwtUtil.generateToken(100L, Role.USER)).thenReturn("jwt-token");

        // Act
        LoginResponse response = userService.login("john", "Secret1!");

        // Assert
        assertEquals("jwt-token", response.getToken());
        assertEquals(100L, response.getUser().getId());
    }

    @Test
    void testLogin_EmailTakesPrecedenceOverUsername() {
        // Arrange: the identifier is one user's email; the username lookup is never needed
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret1!", "$2a$hashed")).thenReturn(true);
        when(jwtUtil.generateToken(100L, Role.USER)).thenReturn("jwt-token");

        // Act
        userService.login("john@example.com", "Secret1!");

        // Assert
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void testLogin_TrimsTheIdentifier() {
        // Arrange
        when(userRepository.findByEmail("john")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret1!", "$2a$hashed")).thenReturn(true);
        when(jwtUtil.generateToken(100L, Role.USER)).thenReturn("jwt-token");

        // Act & Assert
        assertEquals("jwt-token", userService.login("  john ", "Secret1!").getToken());
    }

    @Test
    void testLogin_WrongPasswordWithUsernameThrowsBadCredentials() {
        // Arrange
        when(userRepository.findByEmail("john")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> userService.login("john", "wrong"));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void testLogin_WrongPasswordThrowsBadCredentials() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        // Act & Assert
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> userService.login("john@example.com", "wrong"));
        // Same message as an unknown user: the response does not reveal which accounts exist
        assertEquals("Invalid credentials", ex.getMessage());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void testLogin_WithRealBCrypt() {
        // Arrange
        BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder();
        user.setPassword(realEncoder.encode("Secret1!"));
        userService = new UserServiceImpl(userRepository, realEncoder, jwtUtil, dtoMapper, entityMapper);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(100L, Role.USER)).thenReturn("token");

        // Act & Assert
        assertEquals("token", userService.login("john@example.com", "Secret1!").getToken());
        assertThrows(BadCredentialsException.class, () -> userService.login("john@example.com", "secret1!"));
    }

    @Test
    void testLogin_UnexpectedErrorIsRethrown() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), any())).thenThrow(new IllegalStateException("boom"));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> userService.login("john@example.com", "Secret1!"));
    }

    // -------- Create --------

    @Test
    void testCreate_EncodesPasswordAndDefaultsRole() {
        // Arrange
        stubFavouritesResolution();
        when(passwordEncoder.encode("Secret1!")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDto result = userService.create(userDto());

        // Assert
        User saved = captureSaved();
        assertEquals("encoded", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());
        assertEquals("jane", result.getUsername());
        // create() does not check uniqueness (unlike register)
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testCreate_NullPasswordIsNotEncoded() {
        // Arrange
        UserDto dto = userDto();
        dto.setPassword(null);
        stubFavouritesResolution();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.create(dto);

        // Assert
        assertNull(captureSaved().getPassword());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testCreate_EmptyPasswordIsNotEncoded() {
        // Arrange
        UserDto dto = userDto();
        dto.setPassword("");
        stubFavouritesResolution();
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.create(dto);

        // Assert
        assertEquals("", captureSaved().getPassword());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testCreate_WithoutFavourites() {
        // Arrange
        UserDto dto = userDto();
        dto.setFavouriteGenresIds(null);
        dto.setFavouriteInstrumentsIds(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDto result = userService.create(dto);

        // Assert
        assertTrue(result.getFavouriteGenresIds().isEmpty());
        assertTrue(result.getFavouriteInstrumentsIds().isEmpty());
        verifyNoInteractions(genreRepository, instrumentRepository);
    }

    @Test
    void testCreate_RepositoryFailureIsRethrown() {
        // Arrange
        stubFavouritesResolution();
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("fail"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.create(userDto()));
    }

    // -------- Queries --------

    @Test
    void testFindByEmail_Found() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // Act
        Optional<UserDto> result = userService.findByEmail("john@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    void testFindByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("x@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.findByEmail("x@example.com").isEmpty());
    }

    @Test
    void testFindAll() {
        // Arrange
        User other = new User();
        other.setId(101L);
        when(userRepository.findAll()).thenReturn(List.of(user, other));

        // Act
        List<UserDto> result = userService.findAll();

        // Assert
        assertEquals(List.of(100L, 101L), result.stream().map(UserDto::getId).toList());
    }

    @Test
    void testFindAll_Empty() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(userService.findAll().isEmpty());
    }

    @Test
    void testFindByIds() {
        // Arrange
        when(userRepository.findAllById(List.of(100L))).thenReturn(List.of(user));

        // Act
        List<UserDto> result = userService.findByIds(List.of(100L));

        // Assert
        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().getId());
    }

    @Test
    void testFindById_Found() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // Act
        Optional<UserDto> result = userService.findById(100L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.findById(1L).isEmpty());
    }

    @Test
    void testFindById_NullId() {
        // Act & Assert
        assertTrue(userService.findById(null).isEmpty());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void testFindFavoriteGenreIdsForUser() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // Act
        Optional<List<Long>> result = userService.findFavoriteGenreIdsForUser(100L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(List.of(1L, 2L), result.get());
    }

    @Test
    void testFindFavoriteGenreIdsForUser_UnknownUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.findFavoriteGenreIdsForUser(1L).isEmpty());
    }

    @Test
    void testFindFavoriteGenreIdsForUser_NoFavourites() {
        // Arrange
        user.getPreferences().setFavoriteGenres(new LinkedHashSet<>());
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // Act
        Optional<List<Long>> result = userService.findFavoriteGenreIdsForUser(100L);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void testFindFavoriteInstrumentIdsForUser() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // Act
        Optional<List<Long>> result = userService.findFavoriteInstrumentIdsForUser(100L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(List.of(10L, 11L), result.get());
    }

    @Test
    void testFindFavoriteInstrumentIdsForUser_UnknownUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.findFavoriteInstrumentIdsForUser(1L).isEmpty());
    }

    // -------- Favourite genres --------

    @Test
    void testAddFavoriteGenre_AddsAndSaves() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(genreRepository.findById(3L)).thenReturn(Optional.of(pop));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.addFavoriteGenre(100L, 3L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(Set.of(1L, 2L, 3L), Set.copyOf(result.get().getFavouriteGenresIds()));
        verify(userRepository).save(user);
    }

    @Test
    void testAddFavoriteGenre_AlreadyFavouriteHasNoEffect() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre(1L, "Rock copy")));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.addFavoriteGenre(100L, 1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getFavouriteGenresIds().size());
    }

    @Test
    void testAddFavoriteGenre_UnknownUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.addFavoriteGenre(1L, 3L).isEmpty());
        verifyNoInteractions(genreRepository);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testAddFavoriteGenre_UnknownGenreThrows() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.addFavoriteGenre(100L, 99L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRemoveFavoriteGenre_AboveMinimumRemoves() {
        // Arrange
        user.getPreferences().getFavoriteGenres().add(pop);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.removeFavoriteGenre(100L, 3L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(Set.of(1L, 2L), Set.copyOf(result.get().getFavouriteGenresIds()));
    }

    @Test
    void testRemoveFavoriteGenre_AtMinimumThrows() {
        // Arrange: exactly MIN_N_FAVOURITE_GENRES favourites
        assertEquals(Preferences.MIN_N_FAVOURITE_GENRES, user.getPreferences().getFavoriteGenres().size());
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.removeFavoriteGenre(100L, 1L));
        assertTrue(ex.getMessage().contains(String.valueOf(Preferences.MIN_N_FAVOURITE_GENRES)));
        assertEquals(2, user.getPreferences().getFavoriteGenres().size());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRemoveFavoriteGenre_NotAFavouriteAtMinimumIsNoOp() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.removeFavoriteGenre(100L, 3L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().getFavouriteGenresIds().size());
    }

    @Test
    void testRemoveFavoriteGenre_UnknownUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.removeFavoriteGenre(1L, 1L).isEmpty());
        verify(userRepository, never()).save(any());
    }

    // -------- Favourite instruments --------

    @Test
    void testAddFavoriteInstrument_AddsAndSaves() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(instrumentRepository.findById(12L)).thenReturn(Optional.of(drums));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.addFavoriteInstrument(100L, 12L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(Set.of(10L, 11L, 12L), Set.copyOf(result.get().getFavouriteInstrumentsIds()));
    }

    @Test
    void testAddFavoriteInstrument_AlreadyFavouriteHasNoEffect() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(instrumentRepository.findById(10L)).thenReturn(Optional.of(instrument(10L, "Guitar copy")));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.addFavoriteInstrument(100L, 10L);

        // Assert
        assertEquals(2, result.orElseThrow().getFavouriteInstrumentsIds().size());
    }

    @Test
    void testAddFavoriteInstrument_UnknownUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.addFavoriteInstrument(1L, 12L).isEmpty());
        verifyNoInteractions(instrumentRepository);
    }

    @Test
    void testAddFavoriteInstrument_UnknownInstrumentThrows() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(instrumentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.addFavoriteInstrument(100L, 99L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRemoveFavoriteInstrument_AboveMinimumRemoves() {
        // Arrange
        user.getPreferences().getFavoriteInstruments().add(drums);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.removeFavoriteInstrument(100L, 10L);

        // Assert
        assertEquals(Set.of(11L, 12L), Set.copyOf(result.orElseThrow().getFavouriteInstrumentsIds()));
    }

    @Test
    void testRemoveFavoriteInstrument_AtMinimumThrows() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.removeFavoriteInstrument(100L, 10L));
        assertTrue(ex.getMessage().contains(String.valueOf(Preferences.MIN_N_FAVOURITE_INSTRUMENTS)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRemoveFavoriteInstrument_NotAFavouriteIsNoOp() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.removeFavoriteInstrument(100L, 12L);

        // Assert
        assertEquals(2, result.orElseThrow().getFavouriteInstrumentsIds().size());
    }

    @Test
    void testRemoveFavoriteInstrument_UnknownUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.removeFavoriteInstrument(1L, 10L).isEmpty());
    }

    // -------- Update --------

    @Test
    void testUpdate_ReplacesProfileAndFavourites() {
        // Arrange
        UserDto dto = userDto();
        dto.setFavouriteGenresIds(List.of(2L, 3L));
        dto.setFavouriteInstrumentsIds(List.of(11L, 12L));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(jazz));
        when(genreRepository.findById(3L)).thenReturn(Optional.of(pop));
        when(instrumentRepository.findById(11L)).thenReturn(Optional.of(piano));
        when(instrumentRepository.findById(12L)).thenReturn(Optional.of(drums));
        when(passwordEncoder.encode("Secret1!")).thenReturn("new-hash");
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.update(100L, dto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("jane", user.getUsername());
        assertEquals("Jane Doe", user.getName());
        assertEquals("https://example.com/jane.png", user.getProfileImageUrl());
        assertEquals("new-hash", user.getPassword());
        assertEquals(Set.of(jazz, pop), user.getPreferences().getFavoriteGenres());
        assertEquals(Set.of(piano, drums), user.getPreferences().getFavoriteInstruments());
        assertEquals(100L, result.get().getId());
    }

    @Test
    void testUpdate_SetsTheDifficultyLevel() {
        // Arrange
        UserDto dto = userDto();
        dto.setDifficultyLevel(3);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.update(100L, dto);

        // Assert
        assertEquals(3, user.getDifficultyLevel());
        assertEquals(Integer.valueOf(3), result.orElseThrow().getDifficultyLevel());
    }

    @Test
    void testUpdate_DifficultyLevelBounds_AreAccepted() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);
        UserDto dto = userDto();

        // Act & Assert
        dto.setDifficultyLevel(0);
        userService.update(100L, dto);
        assertEquals(0, user.getDifficultyLevel());

        dto.setDifficultyLevel(5);
        userService.update(100L, dto);
        assertEquals(5, user.getDifficultyLevel());
    }

    @Test
    void testUpdate_NullDifficultyLevel_KeepsTheCurrentOne() {
        // Arrange: a client that does not send the level must not reset it
        user.setDifficultyLevel(4);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.update(100L, userDto());

        // Assert
        assertEquals(4, user.getDifficultyLevel());
    }

    @Test
    void testUpdate_DifficultyLevelOutOfRange_ThrowsAndDoesNotSave() {
        // Arrange
        user.setDifficultyLevel(2);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();

        for (int invalid : new int[] {-1, 6}) {
            UserDto dto = userDto();
            dto.setDifficultyLevel(invalid);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.update(100L, dto));
            assertEquals("Difficulty level must be between 0 and 5", ex.getMessage());
        }
        assertEquals(2, user.getDifficultyLevel());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdate_IgnoresTheEmail() {
        // Arrange: the email was verified at registration, so a new one in the DTO is not applied
        String verifiedEmail = user.getEmail();
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.update(100L, userDto());

        // Assert
        assertNotEquals("jane@example.com", verifiedEmail);
        assertEquals(verifiedEmail, user.getEmail());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testUpdate_EmailOfAnotherUser_IsIgnoredInsteadOfRejected() {
        // Arrange: the email is not applied, so it does not matter that it belongs to someone else
        User other = new User();
        other.setId(200L);
        lenient().when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(other));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act & Assert
        assertTrue(userService.update(100L, userDto()).isPresent());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void testUpdate_UsernameOfAnotherUser_ThrowsAndDoesNotSave() {
        // Arrange
        User other = new User();
        other.setId(200L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(other));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.update(100L, userDto()));
        assertEquals("Username already taken", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdate_KeepingOwnUsername_IsAllowed() {
        // Arrange: the username found belongs to the user being updated
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act & Assert
        assertTrue(userService.update(100L, userDto()).isPresent());
    }

    @Test
    void testUpdate_DuplicatedGenreIds_BelowMinimum_Throws() {
        // Arrange (regression: [3, 3] passes the DTO @Size but resolves to a single favourite)
        UserDto dto = userDto();
        dto.setFavouriteGenresIds(List.of(3L, 3L));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(genreRepository.findById(3L)).thenReturn(Optional.of(pop));
        when(instrumentRepository.findById(10L)).thenReturn(Optional.of(guitar));
        when(instrumentRepository.findById(11L)).thenReturn(Optional.of(piano));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.update(100L, dto));
        assertEquals("A user must have at least " + Preferences.MIN_N_FAVOURITE_GENRES + " favourite genres",
                ex.getMessage());
        assertEquals(Set.of(rock, jazz), user.getPreferences().getFavoriteGenres());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdate_NoFavouriteInstruments_Throws() {
        // Arrange
        UserDto dto = userDto();
        dto.setFavouriteInstrumentsIds(List.of());
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(genreRepository.findById(1L)).thenReturn(Optional.of(rock));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(jazz));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.update(100L, dto));
        assertEquals("A user must have at least " + Preferences.MIN_N_FAVOURITE_INSTRUMENTS + " favourite instruments",
                ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdate_NullPasswordKeepsExistingHash() {
        // Arrange
        UserDto dto = userDto();
        dto.setPassword(null);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.update(100L, dto);

        // Assert
        assertEquals("$2a$hashed", user.getPassword());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testUpdate_EmptyPasswordKeepsExistingHash() {
        // Arrange
        UserDto dto = userDto();
        dto.setPassword("");
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.update(100L, dto);

        // Assert
        assertEquals("$2a$hashed", user.getPassword());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testUpdate_NullRoleDefaultsToUser() {
        // Arrange
        user.setRole(null);
        UserDto dto = userDto();
        dto.setPassword(null);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.update(100L, dto);

        // Assert
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void testUpdate_ExistingRoleIsKept() {
        // Arrange
        user.setRole(Role.ADMIN);
        UserDto dto = userDto();
        dto.setPassword(null);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.update(100L, dto);

        // Assert
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.update(1L, userDto());

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(genreRepository, instrumentRepository, passwordEncoder);
    }

    @Test
    void testUpdate_UnknownGenreThrows() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.update(100L, userDto()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdate_KeepsSpotifyConnection() {
        // Arrange
        user.setSpotifyUser(new SpotifyUser());
        UserDto dto = userDto();
        dto.setPassword(null);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        stubFavouritesResolution();
        when(userRepository.save(user)).thenReturn(user);

        // Act
        Optional<UserDto> result = userService.update(100L, dto);

        // Assert
        assertTrue(result.orElseThrow().isSpotifyConnected());
    }

    // -------- Delete --------

    @Test
    void testDelete_Exists() {
        // Arrange
        when(userRepository.existsById(100L)).thenReturn(true);

        // Act & Assert
        assertTrue(userService.delete(100L));
        verify(userRepository).deleteById(100L);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertFalse(userService.delete(1L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testDelete_RepositoryFailureIsRethrown() {
        // Arrange
        when(userRepository.existsById(100L)).thenReturn(true);
        doThrow(new RuntimeException("constraint")).when(userRepository).deleteById(100L);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.delete(100L));
    }

    @Test
    void testFavouritesListIsMutableCopy() {
        // Arrange
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        // Act
        List<Long> ids = new ArrayList<>(userService.findFavoriteGenreIdsForUser(100L).orElseThrow());
        ids.add(999L);

        // Assert: the user's preferences are untouched
        assertEquals(2, user.getPreferences().getFavoriteGenres().size());
    }

    // -------- Top artists --------

    @Test
    void testFindTopArtistsForUser_DelegatesToTheUsersLists() {
        // Arrange
        User owner = mock(User.class);
        when(owner.getTopArtistsFromItemLists(3)).thenReturn(List.of("Queen", "Bowie"));
        when(userRepository.findById(100L)).thenReturn(Optional.of(owner));

        // Act & Assert
        assertEquals(Optional.of(List.of("Queen", "Bowie")), userService.findTopArtistsForUser(100L, 3));
    }

    @Test
    void testFindTopArtistsForUser_MissingUserReturnsEmpty() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertTrue(userService.findTopArtistsForUser(999L, 5).isEmpty());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {0, -1, 11})
    void testFindTopArtistsForUser_TotalOutOfRangeIsRejected(int total) {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.findTopArtistsForUser(100L, total));
        verify(userRepository, never()).findById(anyLong());
    }
}
