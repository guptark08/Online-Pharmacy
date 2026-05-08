package com.example.demo.repository;

import com.example.demo.TestApplication;
import com.example.demo.entitty.Category;
import com.example.demo.entitty.Medicine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MedicineRepositoryTest {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByIsActiveTrue_ShouldReturnActiveMedicines() {
        // Given
        Category category = new Category();
        category.setName("Test Category");
        category.setActive(true);
        category = categoryRepository.save(category);

        Medicine medicine1 = new Medicine();
        medicine1.setName("Active Medicine");
        medicine1.setDescription("Active");
        medicine1.setCategory(category);
        medicine1.setManufacturer("Test Manufacturer");
        medicine1.setPrice(BigDecimal.valueOf(10.00));
        medicine1.setStock(100);
        medicine1.setActive(true);

        Medicine medicine2 = new Medicine();
        medicine2.setName("Inactive Medicine");
        medicine2.setDescription("Inactive");
        medicine2.setCategory(category);
        medicine2.setManufacturer("Test Manufacturer");
        medicine2.setPrice(BigDecimal.valueOf(20.00));
        medicine2.setStock(50);
        medicine2.setActive(false);

        medicineRepository.save(medicine1);
        medicineRepository.save(medicine2);

        // When
        List<Medicine> activeMedicines = medicineRepository.findByIsActiveTrue();

        // Then
        assertThat(activeMedicines).hasSize(1);
        assertThat(activeMedicines.get(0).getName()).isEqualTo("Active Medicine");
    }

    @Test
    void findById_ShouldReturnMedicine() {
        // Given
        Category category = new Category();
        category.setName("Test Category");
        category.setActive(true);
        category = categoryRepository.save(category);

        Medicine medicine = new Medicine();
        medicine.setName("Test Medicine");
        medicine.setDescription("Test");
        medicine.setCategory(category);
        medicine.setManufacturer("Test Manufacturer");
        medicine.setPrice(BigDecimal.valueOf(15.00));
        medicine.setStock(75);
        medicine.setActive(true);

        Medicine saved = medicineRepository.save(medicine);

        // When
        Optional<Medicine> found = medicineRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Medicine");
    }

    @Test
    void save_ShouldPersistMedicine() {
        // Given
        Category category = new Category();
        category.setName("Test Category");
        category.setActive(true);
        category = categoryRepository.save(category);

        Medicine medicine = new Medicine();
        medicine.setName("New Medicine");
        medicine.setDescription("New");
        medicine.setManufacturer("Test Manufacturer");
        medicine.setCategory(category);
        medicine.setPrice(BigDecimal.valueOf(25.00));
        medicine.setStock(200);
        medicine.setActive(true);

        // When
        Medicine saved = medicineRepository.save(medicine);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Medicine");
    }
}
