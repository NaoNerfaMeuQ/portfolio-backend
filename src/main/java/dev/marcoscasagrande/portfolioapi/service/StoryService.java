package dev.marcoscasagrande.portfolioapi.service;

import dev.marcoscasagrande.portfolioapi.dto.StoryRequestDTO;
import dev.marcoscasagrande.portfolioapi.dto.StoryResponseDTO;
import dev.marcoscasagrande.portfolioapi.model.Story;
import dev.marcoscasagrande.portfolioapi.repository.StoryRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StoryService {

    private static final Safelist STORY_HTML_SAFELIST = Safelist.relaxed()
            .addTags("figure", "figcaption")
            .addAttributes("img", "alt", "class")
            .addProtocols("img", "src", "https")
            .addProtocols("a", "href", "http", "https", "mailto");

    private static final Document.OutputSettings HTML_OUTPUT_SETTINGS =
            new Document.OutputSettings().prettyPrint(false);

    private final StoryRepository storyRepository;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    /**
     * Retorna stories paginadas com limite de proteção de payload (máximo 50 itens por página).
     */
    public Page<StoryResponseDTO> getStoriesPaged(String category, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50); // Trava máxima contra DoS

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "publishedAt"));

        if (category != null && !category.isBlank()) {
            return storyRepository.findByCategoryOrderByPublishedAtDesc(category.trim(), pageable)
                    .map(StoryResponseDTO::new);
        }

        return storyRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(StoryResponseDTO::new);
    }

    public List<StoryResponseDTO> getAllStories() {
        return storyRepository.findAllByOrderByPublishedAtDesc()
                .stream()
                .map(StoryResponseDTO::new)
                .toList();
    }

    public List<StoryResponseDTO> getStoriesByCategory(String category) {
        return storyRepository.findByCategoryOrderByPublishedAtDesc(category)
                .stream()
                .map(StoryResponseDTO::new)
                .toList();
    }

    public Optional<StoryResponseDTO> getStoryById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return storyRepository.findById(id)
                .map(StoryResponseDTO::new);
    }

    public StoryResponseDTO createStory(StoryRequestDTO dto) {
        Story story = new Story();
        story.setTitle(dto.title().trim());
        story.setSubtitle(dto.subtitle() != null ? dto.subtitle().trim() : null);
        story.setContent(sanitizeContent(dto.content()));
        story.setImageUrl(dto.imageUrl() != null ? dto.imageUrl().trim() : null);
        story.setCategory(dto.category() != null ? dto.category().trim() : null);
        story.setPublishedAt(LocalDateTime.now());

        Story savedStory = storyRepository.save(story);
        return new StoryResponseDTO(savedStory);
    }

    public Optional<StoryResponseDTO> updateStory(String id, StoryRequestDTO dto) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return storyRepository.findById(id).map(existingStory -> {
            existingStory.setTitle(dto.title().trim());
            existingStory.setSubtitle(dto.subtitle() != null ? dto.subtitle().trim() : null);
            existingStory.setContent(sanitizeContent(dto.content()));
            existingStory.setImageUrl(dto.imageUrl() != null ? dto.imageUrl().trim() : null);
            existingStory.setCategory(dto.category() != null ? dto.category().trim() : null);

            Story updatedStory = storyRepository.save(existingStory);
            return new StoryResponseDTO(updatedStory);
        });
    }

    public boolean deleteStory(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        if (storyRepository.existsById(id)) {
            storyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private String sanitizeContent(String content) {
        String sanitized = Jsoup.clean(content.trim(), "", STORY_HTML_SAFELIST, HTML_OUTPUT_SETTINGS).trim();
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("O conteúdo precisa conter texto ou HTML permitido.");
        }
        return sanitized;
    }
}
