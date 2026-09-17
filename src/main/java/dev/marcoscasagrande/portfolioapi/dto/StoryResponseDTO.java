package dev.marcoscasagrande.portfolioapi.dto;

import dev.marcoscasagrande.portfolioapi.model.Story;

import java.time.LocalDateTime;

public record StoryResponseDTO(
        String id,
        String title,
        String subtitle,
        String content,
        String imageUrl,
        String category,
        LocalDateTime publishedAt
) {
    public StoryResponseDTO(Story story) {
        this(
                story.getId(),
                story.getTitle(),
                story.getSubtitle(),
                story.getContent(),
                story.getImageUrl(),
                story.getCategory(),
                story.getPublishedAt()
        );
    }
}