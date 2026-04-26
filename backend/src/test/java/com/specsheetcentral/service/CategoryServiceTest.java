package com.specsheetcentral.service;

import com.specsheetcentral.model.Category;
import com.specsheetcentral.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAll_returnsAllCategories() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Sensors");
        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        List<Category> result = categoryService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sensors");
    }

    @Test
    void findById_existingId_returnsCategory() {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Sensors");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));

        Category result = categoryService.findById(1L);

        assertThat(result.getName()).isEqualTo("Sensors");
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Category not found");
    }

    @Test
    void create_savesAndReturnsCategory() {
        Category cat = new Category();
        cat.setName("Displays");
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Category result = categoryService.create(cat);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Displays");
    }

    @Test
    void update_existingCategory_updatesName() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setName("Old Name");

        Category updated = new Category();
        updated.setName("New Name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void delete_callsRepositoryDelete() {
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }
}