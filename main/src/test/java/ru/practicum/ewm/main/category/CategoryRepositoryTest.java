package ru.practicum.ewm.main.category;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryRepositoryTest {

    @Test
    void shouldSaveCategory() {
        CategoryRepository repository = mock(CategoryRepository.class);
        Category category = Category.builder().name("Концерты").build();
        Category savedCategory = Category.builder().id(1L).name("Концерты").build();

        when(repository.save(category)).thenReturn(savedCategory);

        Category result = repository.save(category);

        assertEquals(1L, result.getId());
        assertEquals("Концерты", result.getName());
        verify(repository, times(1)).save(category);
    }

    @Test
    void shouldFindCategoryById() {
        CategoryRepository repository = mock(CategoryRepository.class);
        Category category = Category.builder().id(1L).name("Концерты").build();

        when(repository.findById(1L)).thenReturn(Optional.of(category));

        Optional<Category> result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Концерты", result.get().getName());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenCategoryNotFound() {
        CategoryRepository repository = mock(CategoryRepository.class);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Category> result = repository.findById(999L);

        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void shouldCheckIfCategoryExistsByName() {
        CategoryRepository repository = mock(CategoryRepository.class);

        when(repository.existsByName("Концерты")).thenReturn(true);
        when(repository.existsByName("Несуществующие")).thenReturn(false);

        assertTrue(repository.existsByName("Концерты"));
        assertFalse(repository.existsByName("Несуществующие"));
        verify(repository, times(1)).existsByName("Концерты");
        verify(repository, times(1)).existsByName("Несуществующие");
    }

    @Test
    void shouldFindAllCategoriesWithPagination() {
        CategoryRepository repository = mock(CategoryRepository.class);
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(
                Category.builder().id(1L).name("Концерты").build(),
                Category.builder().id(2L).name("Выставки").build()
        );
        Page<Category> page = new PageImpl<>(categories, pageable, categories.size());

        when(repository.findAll(pageable)).thenReturn(page);

        Page<Category> result = repository.findAll(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals("Концерты", result.getContent().get(0).getName());
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void shouldDeleteCategoryById() {
        CategoryRepository repository = mock(CategoryRepository.class);

        repository.deleteById(1L);

        verify(repository, times(1)).deleteById(1L);
    }
}