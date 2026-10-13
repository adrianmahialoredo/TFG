package com.uo289390.toco.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.uo289390.toco.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.uo289390.toco.config.SecurityConfig;
import com.uo289390.toco.dto.LoginRequestDto;
import com.uo289390.toco.dto.LoginResponse;
import com.uo289390.toco.dto.UserDto;
import com.uo289390.toco.dto.UserResponseDTO;
import com.uo289390.toco.security.JwtAuthenticationFilter;
import com.uo289390.toco.security.JwtUtil;
import com.uo289390.toco.service.EmailVerificationService;
import com.uo289390.toco.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web layer tests for {@link AuthController}.
 * Covers the email verification code, registration and login, including request validation
 * and error mapping.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    private static final String VALID_PASSWORD = "Password1!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    private UserDto validRegistration() {
        UserDto dto = new UserDto();
        dto.setEmail("john@example.com");
        dto.setName("John Doe");
        dto.setUsername("johnny");
        dto.setPassword(VALID_PASSWORD);
        dto.setFavouriteGenresIds(List.of(1L, 2L));
        dto.setFavouriteInstrumentsIds(List.of(3L, 4L));
        return dto;
    }

    private LoginRequestDto validLogin() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("john@example.com");
        dto.setPassword(VALID_PASSWORD);
        return dto;
    }

    private LoginResponse loginResponse() {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(7L);
        user.setEmail("john@example.com");
        user.setUsername("johnny");
        user.setName("John Doe");
        user.setFavouriteGenresIds(List.of(1L, 2L));
        user.setFavouriteInstrumentsIds(List.of(3L, 4L));
        return new LoginResponse("jwt-token", user);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    // ---------- verification code ----------

    @Test
    void verificationCode_validEmail_sendsCodeAndReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\"}"))
                .andExpect(status().isNoContent());

        verify(emailVerificationService).sendCode("john@example.com");
    }

    @Test
    void verificationCode_invalidEmail_returnsBadRequestWithoutSending() throws Exception {
        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void verificationCode_emailAlreadyRegistered_returnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Email already registered"))
                .when(emailVerificationService).sendCode("john@example.com");

        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void verificationCode_requestedTooSoon_returnsTooManyRequests() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Verification code requested too recently"))
                .when(emailVerificationService).sendCode("john@example.com");

        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\"}"))
                .andExpect(status().isTooManyRequests());
    }

    // ---------- register ----------

    @Test
    void register_checksCodeBeforeRegisteringAndConsumesItAfter() throws Exception {
        when(userService.login(anyString(), anyString())).thenReturn(loginResponse());
        // verificationCode is write-only, so the DTO cannot be serialized with it: added by hand
        String body = json(validRegistration()).replaceFirst("\\{","{\"verificationCode\":\"123456\",");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        InOrder order = inOrder(emailVerificationService, userService);
        order.verify(emailVerificationService).checkCode("john@example.com", "123456");
        order.verify(userService).register(any(UserDto.class));
        order.verify(emailVerificationService).consume("john@example.com");
    }

    @Test
    void register_wrongCode_returnsBadRequestWithoutRegistering() throws Exception {
        doThrow(new IllegalArgumentException("Invalid verification code"))
                .when(emailVerificationService).checkCode(anyString(), any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRegistration())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid verification code"));

        verify(userService, never()).register(any());
        verify(emailVerificationService, never()).consume(anyString());
    }

    @Test
    void register_rejectedByUserService_keepsTheCode() throws Exception {
        doThrow(new IllegalArgumentException("Username already taken")).when(userService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRegistration())))
                .andExpect(status().isBadRequest());

        verify(emailVerificationService, never()).consume(anyString());
    }

    @Test
    void register_validUser_returnsCreatedWithTokenAndUser() throws Exception {
        when(userService.login("john@example.com", VALID_PASSWORD)).thenReturn(loginResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRegistration())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(7))
                .andExpect(jsonPath("$.user.email").value("john@example.com"))
                .andExpect(jsonPath("$.user.username").value("johnny"))
                .andExpect(jsonPath("$.user.spotifyConnected").value(false))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void register_registersBeforeLoggingInWithSameCredentials() throws Exception {
        when(userService.login(anyString(), anyString())).thenReturn(loginResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRegistration())))
                .andExpect(status().isCreated());

        InOrder order = inOrder(userService);
        order.verify(userService).register(any(UserDto.class));
        order.verify(userService).login("john@example.com", VALID_PASSWORD);
    }

    @Test
    void register_missingPassword_returnsBadRequestWithFieldError() throws Exception {
        UserDto dto = validRegistration();
        dto.setPassword(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));

        verifyNoInteractions(userService);
    }

    @Test
    void register_invalidEmail_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"));

        verifyNoInteractions(userService);
    }

    @Test
    void register_blankEmail_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setEmail("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Email is required"));
    }

    @Test
    void register_tooShortName_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setName("J");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name must be between 2 and 100 characters"));
    }

    @Test
    void register_tooShortUsername_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setUsername("jo");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username must be between 3 and 20 characters"));
    }

    @Test
    void register_weakPassword_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setPassword("password1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value(
                        "Password must contain an uppercase letter, a digit and a symbol, and no spaces"));
    }

    @Test
    void register_invalidProfileImageUrl_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setProfileImageUrl("ftp://example.com/img.png");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.profileImageUrl").exists());
    }

    @Test
    void register_notEnoughFavouriteGenres_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setFavouriteGenresIds(List.of(1L));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.favouriteGenresIds").value("At least 2 favourite genres are required"));
    }

    @Test
    void register_missingFavouriteInstruments_returnsBadRequest() throws Exception {
        UserDto dto = validRegistration();
        dto.setFavouriteInstrumentsIds(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.favouriteInstrumentsIds").value("Favourite instruments are required"));
    }

    @Test
    void register_serviceRejectsDuplicate_returnsBadRequestWithMessage() throws Exception {
        when(userService.register(any(UserDto.class)))
                .thenThrow(new IllegalArgumentException("Email already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRegistration())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already in use"));

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    void register_wrongHttpMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/auth/register"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    // ---------- login ----------

    @Test
    void login_validCredentials_returnsOkWithToken() throws Exception {
        when(userService.login("john@example.com", VALID_PASSWORD)).thenReturn(loginResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validLogin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.id").value(7))
                .andExpect(jsonPath("$.user.favouriteGenresIds[0]").value(1));
    }

    @Test
    void login_badCredentials_returnsUnauthorized() throws Exception {
        when(userService.login(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validLogin())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_invalidEmail_returnsBadRequest() throws Exception {
        LoginRequestDto dto = validLogin();
        dto.setEmail("john");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"));

        verifyNoInteractions(userService);
    }

    @Test
    void login_missingPassword_returnsBadRequest() throws Exception {
        LoginRequestDto dto = validLogin();
        dto.setPassword(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void login_passwordAcceptedAtRegistrationWithAnySymbol_reachesService() throws Exception {
        // Regression: "#" is valid at registration but used to be rejected by the login pattern
        LoginRequestDto dto = validLogin();
        dto.setPassword("Password1#");
        when(userService.login("john@example.com", "Password1#")).thenReturn(loginResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_weakPassword_isNotValidatedButCheckedAgainstCredentials() throws Exception {
        LoginRequestDto dto = validLogin();
        dto.setPassword("Password!");
        when(userService.login(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_malformedJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or missing request body"));

        verifyNoInteractions(userService);
    }

    @Test
    void login_doesNotRequireAuthenticationHeader() throws Exception {
        when(userService.login(anyString(), anyString())).thenReturn(loginResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validLogin())))
                .andExpect(status().isOk());

        verifyNoInteractions(jwtUtil);
    }
}
