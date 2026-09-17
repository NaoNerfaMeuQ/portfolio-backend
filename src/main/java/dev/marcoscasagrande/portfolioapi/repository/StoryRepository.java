package dev.marcoscasagrande.portfolioapi.repository;

import dev.marcoscasagrande.portfolioapi.model.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends MongoRepository<Story, String> {
    List<Story> findAllByOrderByPublishedAtDesc();
    List<Story> findByCategoryOrderByPublishedAtDesc(String category);

    Page<Story> findAllByOrderByPublishedAtDesc(Pageable pageable);
    Page<Story> findByCategoryOrderByPublishedAtDesc(String category, Pageable pageable);
}
