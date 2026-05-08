package com.example.demo.contoller;

import com.example.demo.contoller.CategoryController;
import com.example.demo.dto.CategoryDTO;
import com.example.demo.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setId(1L);
        testCategoryDTO.setName("Pain Relief");
        testCategoryDTO.setDescription("Pain relief medications");
    }

    @Test
    void getAllCategories() {
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategoryDTO));

        ResponseEntity<List<CategoryDTO>> response = categoryController.getAllCategories();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(categoryService).getAllCategories();
    }

    @Test
    void getCategoryById() {
        when(categoryService.getCategoryById(1L)).thenReturn(testCategoryDTO);

        ResponseEntity<CategoryDTO> response = categoryController.getCategoryById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Pain Relief", response.getBody().getName());
        verify(categoryService).getCategoryById(1L);
    }
}