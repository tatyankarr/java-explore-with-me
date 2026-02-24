package ru.practicum.ewm.main.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.dto.NewCategoryDto;
import ru.practicum.ewm.main.category.service.CategoryServiceImpl;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private NewCategoryDto newCategoryDto;
    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        newCategoryDto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();
    }

    @Test
    void addCategory_ShouldAddCategorySuccessfully() {
        when(categoryRepository.existsByName("Концерты")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.addCategory(newCategoryDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Концерты", result.getName());
        verify(categoryRepository, times(1)).existsByName("Концерты");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void addCategory_ShouldThrowConflictException_WhenCategoryNameExists() {
        when(categoryRepository.existsByName("Концерты")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.addCategory(newCategoryDto));

        assertEquals("Category with this name already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void addCategory_ShouldThrowConflictException_WhenDataIntegrityViolationOccurs() {
        when(categoryRepository.existsByName("Концерты")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenThrow(DataIntegrityViolationException.class);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.addCategory(newCategoryDto));

        assertEquals("Category name must be unique", exception.getMessage());
    }

    @Test
    void updateCategory_ShouldUpdateCategorySuccessfully() {
        CategoryDto updateDto = CategoryDto.builder()
                .id(1L)
                .name("Выставки")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Выставки")).thenReturn(false);

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Выставки", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByName("Выставки");
    }

    @Test
    void updateCategory_ShouldUpdateSuccessfully_WhenNameNotChanged() {
        CategoryDto updateDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertNotNull(result);
        assertEquals("Концерты", result.getName());
        verify(categoryRepository, never()).existsByName(anyString());
    }

    @Test
    void updateCategory_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.updateCategory(999L, categoryDto));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void updateCategory_ShouldThrowConflictException_WhenNameAlreadyExists() {
        CategoryDto updateDto = CategoryDto.builder()
                .id(1L)
                .name("Выставки")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Выставки")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.updateCategory(1L, updateDto));

        assertEquals("Category with this name already exists", exception.getMessage());
    }

    @Test
    void deleteCategory_ShouldDeleteCategorySuccessfully() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).existsById(1L);
        verify(eventRepository, times(1)).existsByCategoryId(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.deleteCategory(999L));

        assertEquals("Category not found", exception.getMessage());
        verify(eventRepository, never()).existsByCategoryId(anyLong());
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_ShouldThrowConflictException_WhenCategoryHasEvents() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.deleteCategory(1L));

        assertEquals("Cannot delete category with existing events", exception.getMessage());
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void getCategories_ShouldReturnListOfCategories() {
        List<Category> categories = List.of(
                Category.builder().id(1L).name("Концерты").build(),
                Category.builder().id(2L).name("Выставки").build()
        );

        when(categoryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(categories));

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Концерты", result.get(0).getName());
        assertEquals("Выставки", result.get(1).getName());
        verify(categoryRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getCategories_ShouldReturnEmptyList_WhenNoCategories() {
        when(categoryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getCategories_ShouldCalculatePageCorrectly() {
        PageImpl<Category> emptyPage = new PageImpl<>(List.of());

        when(categoryRepository.findAll(PageRequest.of(2, 10))).thenReturn(emptyPage);
        categoryService.getCategories(20, 10);
        verify(categoryRepository, times(1)).findAll(PageRequest.of(2, 10));

        when(categoryRepository.findAll(PageRequest.of(1, 5))).thenReturn(emptyPage);
        categoryService.getCategories(5, 5);
        verify(categoryRepository, times(1)).findAll(PageRequest.of(1, 5));

        when(categoryRepository.findAll(PageRequest.of(0, 20))).thenReturn(emptyPage);
        categoryService.getCategories(0, 20);
        verify(categoryRepository, times(1)).findAll(PageRequest.of(0, 20));
    }

    @Test
    void getCategory_ShouldReturnCategory_WhenExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getCategory(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Концерты", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategory_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> categoryService.getCategory(999L));

        assertEquals("Category not found", exception.getMessage());
    }
}