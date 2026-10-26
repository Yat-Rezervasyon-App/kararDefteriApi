
package com.tein.karardefteri.project.controller;

import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.service.DevelopmentProjectCreator;
import com.tein.karardefteri.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@ActiveProfiles("dev")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private DevelopmentProjectCreator developmentProjectCreator;

    @Test
    void createProjectReturnsCreatedProject() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(developmentProjectCreator.getUserId()).thenReturn(userId);
        when(projectService.create(any(ProjectRequestDto.class), eq(userId)))
                .thenReturn(new ProjectResponseDto(
                        projectId,
                        "Karar Defteri",
                        "Toplantı kararları",
                        userId,
                        false,
                        Instant.parse("2026-10-26T08:00:00Z"),
                        Instant.parse("2026-10-26T08:00:00Z")
                ));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Karar Defteri",
                                  "description": "Toplantı kararları"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Karar Defteri"))
                .andExpect(jsonPath("$.createdBy").value(userId.toString()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(projectService).create(any(ProjectRequestDto.class), eq(userId));
    }

    @Test
    void createProjectRejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "description": "Geçersiz proje"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listProjectsReturnsProjects() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectService.findAll()).thenReturn(List.of(
                new ProjectResponseDto(
                        projectId,
                        "Karar Defteri",
                        "Toplantı kararları",
                        userId,
                        false,
                        Instant.parse("2026-10-26T08:00:00Z"),
                        Instant.parse("2026-10-26T08:00:00Z")
                )
        ));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Karar Defteri"))
                .andExpect(jsonPath("$[0].createdBy").value(userId.toString()))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }
}
