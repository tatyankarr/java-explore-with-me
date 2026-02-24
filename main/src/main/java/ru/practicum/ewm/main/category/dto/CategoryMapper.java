package ru.practicum.ewm.main.category.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.category.Category;

@UtilityClass
public class CategoryMapper {
    public Category toCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
