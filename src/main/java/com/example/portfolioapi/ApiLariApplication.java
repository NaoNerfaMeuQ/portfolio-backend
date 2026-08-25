package com.example.portfolioapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class ApiLariApplication {

    private static final Logger log = LoggerFactory.getLogger(ApiLariApplication.class);

    public static void main(String[] args) {
        loadLocalEnvFilesIfPresent();
        SpringApplication.run(ApiLariApplication.class, args);
    }

    /**
     * Carrega automaticamente variáveis de ambiente locais caso existam (.env, atlas-credentials.env, etc.)
     * sem expor nada no repositório.
     */
    private static void loadLocalEnvFilesIfPresent() {
        String[] candidatePaths = {
                ".env",
                "atlas-credentials.env",
                "src/main/atlas-credentials.env",
                "src/main/resources/.env"
        };

        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                log.info("Carregando configuracoes de ambiente locais de {}", file.getPath());
                try {
                    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                            continue;
                        }
                        int eqIdx = trimmed.indexOf('=');
                        String key = trimmed.substring(0, eqIdx).trim();
                        String value = trimmed.substring(eqIdx + 1).trim();

                        // Remove aspas caso estejam presentes no arquivo .env
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        // Registra como System Property apenas se não estiver definida
                        if (System.getenv(key) == null && System.getProperty(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                } catch (IOException e) {
                    log.warn("Nao foi possivel ler o arquivo de ambiente {}: {}", path, e.getMessage());
                }
            }
        }
    }
}
