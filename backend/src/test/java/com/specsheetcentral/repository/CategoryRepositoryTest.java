package com.specsheetcentral.repository;

import com.specsheetcentral.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveAndFindCategory() {
        Category cat = new Category();
        cat.setName("Microcontrollers");
        Category saved = categoryRepository.save(cat);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Microcontrollers");

        Category found = categoryRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Microcontrollers");
    }

    @Test
    void shouldFindAllCategories() {
        Category cat1 = new Category();
        cat1.setName("Microcontrollers");
        categoryRepository.save(cat1);

        Category cat2 = new Category();
        cat2.setName("Sensors");
        categoryRepository.save(cat2);

        assertThat(categoryRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldDeleteCategory() {
        Category cat = new Category();
        cat.setName("Displays");
        Category saved = categoryRepository.save(cat);

        categoryRepository.deleteById(saved.getId());
        assertThat(categoryRepository.findById(saved.getId())).isNotPresent();
    }
}
