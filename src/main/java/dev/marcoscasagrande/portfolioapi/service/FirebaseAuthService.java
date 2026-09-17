package dev.marcoscasagrande.portfolioapi.service;

import dev.marcoscasagrande.portfolioapi.exception.ForbiddenException;
import dev.marcoscasagrande.portfolioapi.exception.UnauthorizedException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FirebaseAuthService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthService.class);

    @Value("${app.security.admin-emails:}")
    private String adminEmailsConfig;

    @Value("${app.security.admin-uids:}")
    private String adminUidsConfig;

    private Set<String> adminEmails = Collections.emptySet();
    private Set<String> adminUids = Collections.emptySet();

    @PostConstruct
    public void init() {
        if (adminEmailsConfig != null && !adminEmailsConfig.isBlank()) {
            this.adminEmails = Arrays.stream(adminEmailsConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());
            log.info("Carregados {} e-mails de administradores autorizados.", adminEmails.size());
        }

        if (adminUidsConfig != null && !adminUidsConfig.isBlank()) {
            this.adminUids = Arrays.stream(adminUidsConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toUnmodifiableSet());
            log.info("Carregados {} UIDs de administradores autorizados.", adminUids.size());
        }

        if (adminEmails.isEmpty() && adminUids.isEmpty()) {
            log.warn("AVISO DE SEGURANÇA: Nenhuma lista de ADMIN_EMAILS ou ADMIN_UIDS foi configurada. Configure a variável de ambiente ADMIN_EMAILS para restringir o acesso administrativo.");
        }
    }

    /**
     * Valida o token JWT do Firebase com checagem de revogação e verifica se o usuário possui privilégios de Admin.
     *
     * @param authHeader Cabeçalho Authorization (esperado: "Bearer <token>")
     * @return FirebaseToken decodificado e validado
     * @throws UnauthorizedException Se o token for inválido, expirado ou revogado
     * @throws ForbiddenException    Se o usuário autenticado não tiver permissão de Administrador
     */
    public FirebaseToken authenticateAndAuthorizeAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token de autenticação ausente ou inválido. Formato esperado: Bearer <token>");
        }

        String idToken = authHeader.substring(7).trim();
        if (idToken.isEmpty()) {
            throw new UnauthorizedException("Token de autenticação não pode ser vazio.");
        }

        FirebaseToken decodedToken;
        try {
            // checkRevoked = true garante que tokens revogados ou usuários deletados no Firebase sejam rejeitados
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken, true);
        } catch (FirebaseAuthException e) {
            log.warn("Falha na validação do token Firebase: [codigo={}] {}", e.getAuthErrorCode(), e.getMessage());
            throw new UnauthorizedException("Token de autenticação inválido, expirado ou revogado.");
        } catch (Exception e) {
            log.error("Erro inesperado durante a verificação do token Firebase: {}", e.getMessage());
            throw new UnauthorizedException("Não foi possível autenticar o token informado.");
        }

        if (!isAdmin(decodedToken)) {
            String userIdentifier = decodedToken.getEmail() != null ? decodedToken.getEmail() : decodedToken.getUid();
            log.warn("Tentativa de acesso não autorizado por usuário não-admin: {}", userIdentifier);
            throw new ForbiddenException("Acesso negado. Apenas administradores autorizados podem realizar esta operação.");
        }

        return decodedToken;
    }

    /**
     * Verifica se o token pertence a um administrador autorizado.
     */
    public boolean isAdmin(FirebaseToken token) {
        if (token == null) {
            return false;
        }

        // 1. Verificação por UID
        if (!adminUids.isEmpty() && token.getUid() != null && adminUids.contains(token.getUid())) {
            return true;
        }

        // 2. Verificação por E-mail
        if (!adminEmails.isEmpty() && token.getEmail() != null) {
            String email = token.getEmail().trim().toLowerCase();
            if (adminEmails.contains(email)) {
                return true;
            }
        }

        // 3. Verificação por Custom Claim (ex: { "admin": true } ou { "role": "admin" })
        Object adminClaim = token.getClaims().get("admin");
        if (Boolean.TRUE.equals(adminClaim)) {
            return true;
        }
        Object roleClaim = token.getClaims().get("role");
        if ("admin".equalsIgnoreCase(String.valueOf(roleClaim))) {
            return true;
        }

        // Fail closed: sem allowlist ou custom claim explícita, o acesso é negado.
        // Isso evita transformar uma falha de configuração em permissão administrativa.
        return false;
    }
}
