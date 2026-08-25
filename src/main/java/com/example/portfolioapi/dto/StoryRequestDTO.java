package com.example.portfolioapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoryRequestDTO(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 200, message = "O título não pode exceder 200 caracteres.")
        String title,

        @Size(max = 300, message = "O subtítulo não pode exceder 300 caracteres.")
        String subtitle,

        @NotBlank(message = "O conteúdo é obrigatório.")
        @Size(max = 50000, message = "O conteúdo não pode exceder 50.000 caracteres.")
        String content,

        @Size(max = 2048, message = "A URL da imagem não pode exceder 2048 caracteres.")
        @Pattern(regexp = "^(https?://.+)?$", message = "A URL da imagem deve ser uma URL válida (http:// ou https://) ou vazia.")
        String imageUrl,

        @Size(max = 50, message = "A categoria não pode exceder 50 caracteres.")
        String category
) {}