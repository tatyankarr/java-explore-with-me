package ru.practicum.ewm.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.category.CategoryRepository;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.dto.CategoryMapper;
import ru.practicum.ewm.main.category.dto.NewCategoryDto;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Category with this name already exists");
        }

        Category category = CategoryMapper.toCategory(newCategoryDto);

        try {
            return CategoryMapper.toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Category name must be unique");
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!category.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Category with this name already exists");
        }

        category.setName(categoryDto.getName());

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {

        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category not found");
        }

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Cannot delete category with existing events");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {

        int page = from / size;

        return categoryRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long catId) {

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        return CategoryMapper.toCategoryDto(category);
    }
}