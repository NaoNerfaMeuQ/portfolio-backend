package dev.marcoscasagrande.portfolioapi.service;

import dev.marcoscasagrande.portfolioapi.exception.ForbiddenException;
import dev.marcoscasagrande.portfolioapi.exception.UnauthorizedException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebaseAuthServiceTest {

    private FirebaseAuthService authService;

    @BeforeEach
    void setUp() {
        authService = new FirebaseAuthService();
        ReflectionTestUtils.setField(authService, "adminEmailsConfig", "admin@larissa.com, marcos@larissa.com");
        ReflectionTestUtils.setField(authService, "adminUidsConfig", "admin-uid-123");
        authService.init();
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando o header for nulo ou sem Bearer")
    void shouldThrowUnauthorizedWhenHeaderIsMissingOrInvalid() {
        assertThrows(UnauthorizedException.class, () -> authService.authenticateAndAuthorizeAdmin(null));
        assertThrows(UnauthorizedException.class, () -> authService.authenticateAndAuthorizeAdmin(""));
        assertThrows(UnauthorizedException.class, () -> authService.authenticateAndAuthorizeAdmin("Basic 123456"));
    }

    @Test
    @DisplayName("Deve identificar usuário admin por e-mail configurado")
    void shouldRecognizeAdminByEmail() {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getEmail()).thenReturn("admin@larissa.com");
        when(token.getClaims()).thenReturn(Map.of());

        assertTrue(authService.isAdmin(token));
    }

    @Test
    @DisplayName("Deve identificar usuário admin por UID configurado")
    void shouldRecognizeAdminByUid() {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("admin-uid-123");
        when(token.getEmail()).thenReturn("outro@email.com");
        when(token.getClaims()).thenReturn(Map.of());

        assertTrue(authService.isAdmin(token));
    }

    @Test
    @DisplayName("Deve identificar usuário admin por Custom Claim 'admin'")
    void shouldRecognizeAdminByCustomClaim() {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("user-qualquer");
        when(token.getEmail()).thenReturn("usuario@comum.com");
        when(token.getClaims()).thenReturn(Map.of("admin", true));

        assertTrue(authService.isAdmin(token));
    }

    @Test
    @DisplayName("Deve rejeitar usuário comum sem privilégio de admin")
    void shouldRejectRegularUser() {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("user-sem-permissao");
        when(token.getEmail()).thenReturn("malicioso@hacker.com");
        when(token.getClaims()).thenReturn(Map.of());

        assertFalse(authService.isAdmin(token));
    }

    @Test
    @DisplayName("Deve negar acesso quando nenhuma allowlist ou custom claim foi configurada")
    void shouldFailClosedWhenAdminConfigurationIsMissing() {
        FirebaseAuthService serviceWithoutAllowlist = new FirebaseAuthService();
        ReflectionTestUtils.setField(serviceWithoutAllowlist, "adminEmailsConfig", "");
        ReflectionTestUtils.setField(serviceWithoutAllowlist, "adminUidsConfig", "");
        serviceWithoutAllowlist.init();

        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("authenticated-user");
        when(token.getEmail()).thenReturn("user@example.com");
        when(token.getClaims()).thenReturn(Map.of());

        assertFalse(serviceWithoutAllowlist.isAdmin(token));
    }
}
