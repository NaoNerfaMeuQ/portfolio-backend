package com.example.portfolioapi.controller;

import com.example.portfolioapi.dto.StoryRequestDTO;
import com.example.portfolioapi.dto.StoryResponseDTO;
import com.example.portfolioapi.exception.ResourceNotFoundException;
import com.example.portfolioapi.service.FirebaseAuthService;
import com.example.portfolioapi.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;
    private final FirebaseAuthService firebaseAuthService;

    public StoryController(StoryService storyService, FirebaseAuthService firebaseAuthService) {
        this.storyService = storyService;
        this.firebaseAuthService = firebaseAuthService;
    }

    /**
     * Leitura pública de histórias (com suporte a filtro por categoria e paginação).
     */
    @GetMapping
    public ResponseEntity<?> getAllStories(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            int pageNum = (page != null) ? page : 0;
            int pageSize = (size != null) ? size : 10;
            return ResponseEntity.ok(storyService.getStoriesPaged(category, pageNum, pageSize));
        }

        List<StoryResponseDTO> stories = (category != null && !category.isBlank())
                ? storyService.getStoriesByCategory(category)
                : storyService.getAllStories();
        return ResponseEntity.ok(stories);
    }

    /**
     * Leitura pública de uma história específica por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoryResponseDTO> getStoryById(@PathVariable String id) {
        return storyService.getStoryById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("História não encontrada com o ID: " + id));
    }

    /**
     * Criação de nova história - Restrita a Administradores autenticados via Firebase.
     */
    @PostMapping
    public ResponseEntity<StoryResponseDTO> createStory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody StoryRequestDTO dto
    ) {
        firebaseAuthService.authenticateAndAuthorizeAdmin(authHeader);
        StoryResponseDTO createdStory = storyService.createStory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStory);
    }

    /**
     * Atualização de história existente - Restrita a Administradores autenticados via Firebase.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StoryResponseDTO> updateStory(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody StoryRequestDTO dto
    ) {
        firebaseAuthService.authenticateAndAuthorizeAdmin(authHeader);
        return storyService.updateStory(id, dto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("História não encontrada para atualização com o ID: " + id));
    }

    /**
     * Exclusão de história - Restrita a Administradores autenticados via Firebase.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        firebaseAuthService.authenticateAndAuthorizeAdmin(authHeader);
        boolean deleted = storyService.deleteStory(id);
        if (!deleted) {
            throw new ResourceNotFoundException("História não encontrada para exclusão com o ID: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}