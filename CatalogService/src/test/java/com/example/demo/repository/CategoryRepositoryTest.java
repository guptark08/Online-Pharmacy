package com.example.demo.repository;

import com.example.demo.TestApplication;
import com.example.demo.entitty.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByIsActiveTrue_ShouldReturnActiveCategories() {
        // Given
        Category category1 = new Category();
        category1.setName("Active Category");
        category1.setDescription("Active");
        category1.setActive(true);

        Category category2 = new Category();
        category2.setName("Inactive Category");
        category2.setDescription("Inactive");
        category2.setActive(false);

        categoryRepository.save(category1);
        categoryRepository.save(category2);

        // When
        List<Category> activeCategories = categoryRepository.findByIsActiveTrue();

        // Then
        assertThat(activeCategories).hasSize(1);
        assertThat(activeCategories.get(0).getName()).isEqualTo("Active Category");
    }

    @Test
    void findById_ShouldReturnCategory() {
        // Given
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test");
        category.setActive(true);

        Category saved = categoryRepository.save(category);

        // When
        Optional<Category> found = categoryRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Category");
    }

    @Test
    void save_ShouldPersistCategory() {
        // Given
        Category category = new Category();
        category.setName("New Category");
        category.setDescription("New");
        category.setActive(true);

        // When
        Category saved = categoryRepository.save(category);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Category");
    }
}