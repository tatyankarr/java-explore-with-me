package ru.practicum.ewm.main.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.dto.NewCategoryDto;
import ru.practicum.ewm.main.category.service.CategoryService;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private NewCategoryDto newCategoryDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        newCategoryDto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();
    }

    @Test
    void addCategory_ShouldReturnCreatedCategory() throws Exception {
        when(categoryService.addCategory(any(NewCategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Концерты")));

        verify(categoryService, times(1)).addCategory(any(NewCategoryDto.class));
    }

    @Test
    void addCategory_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        NewCategoryDto invalidDto = NewCategoryDto.builder()
                .name("")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).addCategory(any());
    }

    @Test
    void addCategory_ShouldReturnBadRequest_WhenNameIsTooLong() throws Exception {
        NewCategoryDto invalidDto = NewCategoryDto.builder()
                .name("a".repeat(51))
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).addCategory(any());
    }

    @Test
    void addCategory_ShouldReturnConflict_WhenNameAlreadyExists() throws Exception {
        when(categoryService.addCategory(any(NewCategoryDto.class)))
                .thenThrow(new ConflictException("Category with this name already exists"));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        CategoryDto updateDto = CategoryDto.builder()
                .name("Выставки")
                .build();

        CategoryDto updatedCategory = CategoryDto.builder()
                .id(1L)
                .name("Выставки")
                .build();

        when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(updatedCategory);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Выставки")));
    }

    @Test
    void updateCategory_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
        when(categoryService.updateCategory(eq(999L), any(CategoryDto.class)))
                .thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(patch("/admin/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory_ShouldReturnConflict_WhenNameAlreadyExists() throws Exception {
        CategoryDto updateDto = CategoryDto.builder()
                .name("Выставки")
                .build();

        when(categoryService.updateCategory(eq(1L), any(CategoryDto.class)))
                .thenThrow(new ConflictException("Category with this name already exists"));

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    void deleteCategory_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Category not found"))
                .when(categoryService).deleteCategory(999L);

        mockMvc.perform(delete("/admin/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_ShouldReturnConflict_WhenCategoryHasEvents() throws Exception {
        doThrow(new ConflictException("Cannot delete category with existing events"))
                .when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void getCategories_ShouldReturnListOfCategories() throws Exception {
        List<CategoryDto> categories = List.of(
                CategoryDto.builder().id(1L).name("Концерты").build(),
                CategoryDto.builder().id(2L).name("Выставки").build()
        );

        when(categoryService.getCategories(0, 10)).thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Концерты")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Выставки")));
    }

    @Test
    void getCategories_ShouldUseDefaultValues() throws Exception {
        List<CategoryDto> categories = List.of(categoryDto);
        when(categoryService.getCategories(0, 10)).thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(categoryService, times(1)).getCategories(0, 10);
    }

    @Test
    void getCategory_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategory(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Концерты")));
    }

    @Test
    void getCategory_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
        when(categoryService.getCategory(999L))
                .thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }
}
