package com.example.demo.service;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.entitty.Category;
import com.example.demo.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Pain Relief");
        testCategory.setDescription("Pain relief medications");
        testCategory.setImageUrl("image.jpg");
        testCategory.setActive(true);
    }

    @Test
    void getAllCategories() {
        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(testCategory));

        List<CategoryDTO> categories = categoryService.getAllCategories();

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Pain Relief", categories.get(0).getName());
        verify(categoryRepository).findByIsActiveTrue();
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        CategoryDTO category = categoryService.getCategoryById(1L);

        assertNotNull(category);
        assertEquals(1L, category.getId());
        assertEquals("Pain Relief", category.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_NotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> categoryService.getCategoryById(999L));

        assertEquals("Category not found: 999", exception.getMessage());
        verify(categoryRepository).findById(999L);
    }

    @Test
    void mapToDTO() {
        CategoryDTO dto = categoryService.mapToDTO(testCategory);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Pain Relief", dto.getName());
        assertEquals("Pain relief medications", dto.getDescription());
        assertEquals("image.jpg", dto.getImageUrl());
    }
}