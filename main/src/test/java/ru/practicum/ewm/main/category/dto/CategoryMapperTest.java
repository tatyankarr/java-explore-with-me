package ru.practicum.ewm.main.category.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.category.Category;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {
    @Test
    @DisplayName("Должен конвертировать NewCategoryDto в Category")
    void shouldConvertNewCategoryDtoToCategory() {
        NewCategoryDto dto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        Category category = CategoryMapper.toCategory(dto);

        assertNotNull(category);
        assertNull(category.getId());
        assertEquals("Концерты", category.getName());
    }

    @Test
    @DisplayName("Должен конвертировать Category в CategoryDto")
    void shouldConvertCategoryToCategoryDto() {
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        CategoryDto dto = CategoryMapper.toCategoryDto(category);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Концерты", dto.getName());
    }

    @Test
    @DisplayName("Должен конвертировать Category с null id в CategoryDto")
    void shouldConvertCategoryWithNullIdToCategoryDto() {
        Category category = Category.builder()
                .id(null)
                .name("Концерты")
                .build();

        CategoryDto dto = CategoryMapper.toCategoryDto(category);

        assertNotNull(dto);
        assertNull(dto.getId());
        assertEquals("Концерты", dto.getName());
    }
}
