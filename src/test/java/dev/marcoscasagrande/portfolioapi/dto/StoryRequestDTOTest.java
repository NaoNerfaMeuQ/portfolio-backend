package dev.marcoscasagrande.portfolioapi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StoryRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar com sucesso um DTO correto")
    void shouldValidateValidDTO() {
        StoryRequestDTO dto = new StoryRequestDTO(
                "Título Válido",
                "Subtítulo Válido",
                "Conteúdo da história seguro",
                "https://exemplo.com/imagem.png",
                "Tecnologia"
        );

        Set<ConstraintViolation<StoryRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Não deve haver violações de validação");
    }

    @Test
    @DisplayName("Deve rejeitar título e conteúdo em branco")
    void shouldRejectBlankRequiredFields() {
        StoryRequestDTO dto = new StoryRequestDTO(
                "",
                "Subtítulo",
                "   ",
                null,
                null
        );

        Set<ConstraintViolation<StoryRequestDTO>> violations = validator.validate(dto);
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Deve rejeitar URL de imagem em formato inválido")
    void shouldRejectInvalidImageUrl() {
        StoryRequestDTO dto = new StoryRequestDTO(
                "Título",
                "Subtítulo",
                "Conteúdo",
                "javascript:alert(1)",
                "Geral"
        );

        Set<ConstraintViolation<StoryRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl")));
    }
}
