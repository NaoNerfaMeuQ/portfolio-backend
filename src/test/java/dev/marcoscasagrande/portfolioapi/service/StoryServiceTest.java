package dev.marcoscasagrande.portfolioapi.service;

import dev.marcoscasagrande.portfolioapi.dto.StoryRequestDTO;
import dev.marcoscasagrande.portfolioapi.model.Story;
import dev.marcoscasagrande.portfolioapi.repository.StoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoryServiceTest {

    private StoryRepository repository;
    private StoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(StoryRepository.class);
        service = new StoryService(repository);
        when(repository.save(any(Story.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Remove scripts e atributos executáveis antes de persistir conteúdo")
    void shouldSanitizeStoryHtml() {
        StoryRequestDTO request = new StoryRequestDTO(
                "Título",
                "Subtítulo",
                "<p>Texto seguro</p><script>alert('xss')</script><img src=\"https://example.com/a.png\" onerror=\"alert(1)\">",
                "https://example.com/capa.png",
                "Infantil"
        );

        String sanitized = service.createStory(request).content();

        assertTrue(sanitized.contains("Texto seguro"));
        assertTrue(sanitized.contains("https://example.com/a.png"));
        assertFalse(sanitized.contains("script"));
        assertFalse(sanitized.contains("onerror"));
    }

    @Test
    @DisplayName("Rejeita conteúdo que fica vazio após a sanitização")
    void shouldRejectContentThatBecomesEmptyAfterSanitization() {
        StoryRequestDTO request = new StoryRequestDTO(
                "Título",
                null,
                "<script>alert('xss')</script>",
                null,
                "Infantil"
        );

        assertThrows(IllegalArgumentException.class, () -> service.createStory(request));
    }
}
