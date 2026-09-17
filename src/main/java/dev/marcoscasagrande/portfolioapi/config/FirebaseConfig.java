package dev.marcoscasagrande.portfolioapi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config.path:}")
    private String configPath;

    @Value("${firebase.config.json:}")
    private String configJson;

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            InputStream serviceAccountStream = resolveServiceAccountStream();
            if (serviceAccountStream != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase inicializado com sucesso via credenciais configuradas.");
            } else {
                // Fallback para Application Default Credentials do Google Cloud
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase inicializado com Application Default Credentials.");
            }
        } catch (Exception e) {
            log.error("Aviso: Nao foi possivel inicializar o Firebase Admin SDK. Verifique as credenciais configuradas.");
        }
    }

    private InputStream resolveServiceAccountStream() {
        try {
            // 1. Variável com caminho explícito para o arquivo JSON
            if (configPath != null && !configPath.trim().isEmpty()) {
                log.info("Carregando credenciais Firebase a partir do caminho externo configurado.");
                return new FileInputStream(configPath.trim());
            }

            // 2. Variável de ambiente contendo o JSON direto (ou codificado em Base64)
            if (configJson != null && !configJson.trim().isEmpty()) {
                String trimmedJson = configJson.trim();
                if (trimmedJson.startsWith("{")) {
                    log.info("Carregando credenciais Firebase a partir de JSON bruto em variável de ambiente.");
                    return new ByteArrayInputStream(trimmedJson.getBytes(StandardCharsets.UTF_8));
                } else {
                    log.info("Carregando credenciais Firebase a partir de Base64 em variável de ambiente.");
                    byte[] decoded = Base64.getDecoder().decode(trimmedJson);
                    return new ByteArrayInputStream(decoded);
                }
            }

            // 3. Fallback para classpath (desenvolvimento local apenas, caso exista)
            ClassPathResource classPathResource = new ClassPathResource("serviceAccountKey.json");
            if (classPathResource.exists()) {
                log.warn("ATENCAO: Carregando serviceAccountKey.json do classpath. Em producao, utilize variaveis de ambiente.");
                return classPathResource.getInputStream();
            }
        } catch (Exception e) {
            log.warn("Nao foi possivel resolver stream de credenciais do Firebase: {}", e.getMessage());
        }
        return null;
    }
}