package dev.marcoscasagrande.portfolioapi.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResponse) {
            // Prevenção contra MIME sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // Prevenção contra Clickjacking
            httpResponse.setHeader("X-Frame-Options", "DENY");

            // Proteção XSS legada para navegadores compatíveis
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // Força HTTPS em conexões subsequentes (HSTS)
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

            // Política de Referrer para evitar vazamento de URLs internas
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Política de Permissões de APIs sensíveis do navegador
            httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

            // Content Security Policy básico para endpoints de API
            httpResponse.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        }

        chain.doFilter(request, response);
    }
}
