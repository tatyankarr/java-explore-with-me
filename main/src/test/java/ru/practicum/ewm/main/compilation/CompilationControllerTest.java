package ru.practicum.ewm.main.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.compilation.service.CompilationService;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompilationController.class)
class CompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    private NewCompilationDto newCompilationDto;
    private CompilationDto compilationDto;

    @BeforeEach
    void setUp() {
        newCompilationDto = NewCompilationDto.builder()
                .title("Летние события")
                .pinned(true)
                .events(List.of(1L, 2L))
                .build();

        compilationDto = CompilationDto.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(List.of())
                .build();
    }

    @Test
    void saveCompilation_ShouldReturnCreatedCompilation() throws Exception {
        when(compilationService.saveCompilation(any(NewCompilationDto.class))).thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Летние события"))
                .andExpect(jsonPath("$.pinned").value(true));

        verify(compilationService, times(1)).saveCompilation(any(NewCompilationDto.class));
    }

    @Test
    void saveCompilation_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        NewCompilationDto invalidDto = NewCompilationDto.builder()
                .title("")
                .pinned(true)
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).saveCompilation(any());
    }

    @Test
    void saveCompilation_ShouldReturnBadRequest_WhenTitleIsTooLong() throws Exception {
        NewCompilationDto invalidDto = NewCompilationDto.builder()
                .title("a".repeat(51))
                .pinned(true)
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).saveCompilation(any());
    }

    @Test
    void updateCompilation_ShouldReturnUpdatedCompilation() throws Exception {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("Обновленный заголовок")
                .pinned(false)
                .build();

        CompilationDto updatedDto = CompilationDto.builder()
                .id(1L)
                .title("Обновленный заголовок")
                .pinned(false)
                .events(List.of())
                .build();

        when(compilationService.updateCompilation(eq(1L), any(UpdateCompilationRequest.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Обновленный заголовок"))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void updateCompilation_ShouldReturnBadRequest_WhenTitleIsTooLong() throws Exception {
        UpdateCompilationRequest invalidRequest = UpdateCompilationRequest.builder()
                .title("a".repeat(51))
                .build();

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(compilationService, never()).updateCompilation(anyLong(), any());
    }

    @Test
    void updateCompilation_ShouldReturnNotFound_WhenCompilationDoesNotExist() throws Exception {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("Новый заголовок")
                .build();

        when(compilationService.updateCompilation(eq(999L), any(UpdateCompilationRequest.class)))
                .thenThrow(new NotFoundException("Compilation not found"));

        mockMvc.perform(patch("/admin/compilations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCompilation_ShouldReturnNoContent() throws Exception {
        doNothing().when(compilationService).deleteCompilation(1L);

        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNoContent());

        verify(compilationService, times(1)).deleteCompilation(1L);
    }

    @Test
    void deleteCompilation_ShouldReturnNotFound_WhenCompilationDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Compilation not found"))
                .when(compilationService).deleteCompilation(999L);

        mockMvc.perform(delete("/admin/compilations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompilations_ShouldReturnListOfCompilations() throws Exception {
        List<CompilationDto> compilations = List.of(
                CompilationDto.builder().id(1L).title("Летние события").pinned(true).build(),
                CompilationDto.builder().id(2L).title("Зимние события").pinned(false).build()
        );

        when(compilationService.getCompilations(null, 0, 10)).thenReturn(compilations);

        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Летние события"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Зимние события"));
    }

    @Test
    void getCompilations_ShouldUseDefaultValues() throws Exception {
        List<CompilationDto> compilations = List.of(compilationDto);
        when(compilationService.getCompilations(null, 0, 10)).thenReturn(compilations);

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(compilationService, times(1)).getCompilations(null, 0, 10);
    }

    @Test
    void getCompilations_ShouldFilterByPinned() throws Exception {
        List<CompilationDto> compilations = List.of(compilationDto);
        when(compilationService.getCompilations(true, 0, 10)).thenReturn(compilations);

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pinned").value(true));

        verify(compilationService, times(1)).getCompilations(true, 0, 10);
    }

    @Test
    void getCompilation_ShouldReturnCompilation() throws Exception {
        when(compilationService.getCompilation(1L)).thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Летние события"))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void getCompilation_ShouldReturnNotFound_WhenCompilationDoesNotExist() throws Exception {
        when(compilationService.getCompilation(999L))
                .thenThrow(new NotFoundException("Compilation not found"));

        mockMvc.perform(get("/compilations/999"))
                .andExpect(status().isNotFound());
    }
}