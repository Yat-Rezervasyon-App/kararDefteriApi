
package com.tein.karardefteri.project.controller;

import static org.mockito.ArgumentMatchers.anyString;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import org.springframework.security.test.context.support.WithMockUser;
import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static  org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@example.com" , roles = "MEMBER")
    void createProjectReturnsCreatedProject() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        when(userService.getCurrentUser("test@example.com"))
                .thenReturn(new UserResponseDTO(
                        userId,
                        "Test kullanıcısı",
                        "test@example.com",
                        UserRole.MEMBER,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));

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
                        .with(user("test@example.com").roles("MEMBER"))
                        .with(csrf())
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
    @WithMockUser(username = "test@example.com" , roles = "MEMBER")
    void createProjectRejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .with(csrf())
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
    @WithMockUser(username = "test@example.com" , roles = "MEMBER")

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

    @Test
    @WithMockUser(username="test@example.com" , roles = "MEMBER")
    void getProjectByIdReturnsProject() throws Exception{

        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProjectResponseDto project = new ProjectResponseDto(
                projectId,
                "Karar defteri",
                "Toplantı kararı",
                userId,
                false,
                Instant.parse("2026-10-26T08:00:00Z"),
                Instant.parse("2026-10-26T08:00:00Z")
        );

        when(projectService.findById(projectId)).thenReturn(project);

        mockMvc.perform(get("/api/projects/" + projectId)
                        .with(user("test@example.com").roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Karar defteri"))
                .andExpect(jsonPath("$.archived").value(false));

        verify(projectService).findById(projectId);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "MEMBER")
    void getArchivedProjectReturnsProject() throws Exception {

        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProjectResponseDto archivedProject = new ProjectResponseDto(
                projectId,
                "Arşivlenmiş Proje",
                "Tamamlanmış proje",
                userId,
                true,
                Instant.parse("2026-10-26T08:00:00Z"),
                Instant.parse("2026-10-26T08:00:00Z")
        );

        when(projectService.findById(projectId))
                .thenReturn(archivedProject);

        mockMvc.perform(get("/api/projects/" + projectId)
                        .with(user("test@example.com").roles("MEMBER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Arşivlenmiş Proje"))
                .andExpect(jsonPath("$.archived").value(true));

        verify(projectService).findById(projectId);
    }

    @Test
    @WithMockUser(username = "test@example.com" , roles = "MEMBER")
    void ownerCanUpdateProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProjectResponseDto updatedProject = new ProjectResponseDto(
                projectId,
                "Güncellenmiş Proje",
                "Yeni Açıklama",
                userId,
                false,
                Instant.parse("2026-10-26T08:00:00Z"),
                Instant.parse("2026-10-26T08:00:00Z")
        );
        when(userService.getCurrentUser("test@example.com"))
                .thenReturn(new UserResponseDTO(
                        userId,
                        "Test kullanıcısı",
                        "test@example.com",
                        UserRole.MEMBER,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));
        when(projectService.update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(userId),
                eq(false)
        )).thenReturn(updatedProject);

        mockMvc.perform(put("/api/projects/" +projectId)
                .with(user("test@example.com").roles("MEMBER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name":"Güncellenmiş Proje",
                        "description":"Yeni Açıklama"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Güncellenmiş Proje"));
        verify(projectService).update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(userId),
                eq(false)
        );
    }

    @Test
    @WithMockUser(username = "admin@example.com" , roles = "ADMIN")
    void adminCanUpdateAnotherUsersProject() throws Exception{
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        ProjectResponseDto updateProject = new ProjectResponseDto(
                projectId,
                "Admin tarafından Güncellendi",
                "Yeni Açıklama",
                UUID.randomUUID(),
                false,
                Instant.parse("2026-10-26T08:00:00Z"),
                Instant.parse("2026-10-26T08:00:00Z")
        );
        when(userService.getCurrentUser("admin@example.com"))
                .thenReturn(new UserResponseDTO(
                        adminId,
                        "Admin kullanıcısı",
                        "admin@examle.com",
                        UserRole.ADMIN,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));
        when(projectService.update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(adminId),
                eq(true)
        )).thenReturn(updateProject);

        mockMvc.perform(put("/api/projects/" + projectId)
                        .with(user("admin@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Admin tarafından Güncellendi",
                              "description": "Yeni açıklama"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Admin tarafından Güncellendi"));

        verify(projectService).update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(adminId),
                eq(true)
        );
    }

    @Test
    @WithMockUser(username = "member@example.com" , roles = "MEMBER")
    void nonOwnerCannotUpdateProject() throws Exception{
        UUID projectId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        when(userService.getCurrentUser("member@example.com"))
                .thenReturn(new UserResponseDTO(
                        memberId,
                        "Member kullanıcısı",
                        "member@example.com",
                        UserRole.MEMBER,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));
        when(projectService.update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(memberId),
                eq(false)
        )).thenThrow(new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bu projeyi güncelleme yetkiniz yok"
        ));
        mockMvc.perform(put("/api/projects/" + projectId)
                .with(user("member@example.com").roles("MEMBER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name":"Başkasının Projesi",
                        "description": "Yetkisiz güncelleme denemesi"
                        }
                        """))
                .andExpect(status().isForbidden());

        verify(projectService).update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(memberId),
                eq(false)
        );
    }

    @Test
    @WithMockUser(username = "owmer@example.com", roles = "MEMBER")
    void ownerCanArchiveProject() throws Exception{
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ProjectResponseDto archivedProject = new ProjectResponseDto(
                projectId,
                "Arşivlenmiş Proje",
                "Proje açıklaması",
                userId,
                true,
                Instant.parse("2026-10-26T08:00:00Z"),
                Instant.parse("2026-10-26T08:00:00Z")
        );

        when(userService.getCurrentUser(anyString()))
                .thenReturn(new UserResponseDTO(
                        userId,
                        "Proje sahibi",
                        "owner@example.com",
                        UserRole.MEMBER,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));

        when(projectService.archive(projectId, userId, false))
                .thenReturn(archivedProject);

        mockMvc.perform(patch("/api/projects/"+projectId+"/archive")
                .with(user("owner@example.com").roles("MEMBER"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.archived").value(true));

        verify(projectService).archive(projectId, userId, false);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminCanArchiveAnotherUsersProject() throws Exception{
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        ProjectResponseDto archivedProject = new ProjectResponseDto(
                projectId,
                "Başka kullanıcının projesi",
                "Admin tarafından arşivlendi",
                UUID.randomUUID(),
                true,
                Instant.parse("2026-10-26T08:00:00Z"),
                Instant.parse("2026-10-26T08:00:00Z")
        );

        when(userService.getCurrentUser(anyString()))
                .thenReturn(new UserResponseDTO(
                        adminId,
                        "Admin kullanıcısı",
                        "admin@example.com",
                        UserRole.ADMIN,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));
        when(projectService.archive(projectId, adminId,true))
                .thenReturn(archivedProject);

        mockMvc.perform(patch("/api/projects/"+projectId+"/archive")
                .with(user("admin@example.com").roles("ADMIN"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));

        verify(projectService).archive(projectId, adminId , true);
    }

    @Test
    @WithMockUser(username = "member@example.com", roles = "MEMBER")
    void nonOwnerCannotArchiveProject() throws Exception{
        UUID projectId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        when(userService.getCurrentUser(anyString()))
                .thenReturn(new UserResponseDTO(
                        memberId,
                        "Member kullanıcısı",
                        "member@example.com",
                        UserRole.MEMBER,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));

        when(projectService.archive(projectId,memberId,false))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Bu projeyi arşivleme yetkiniz yok"
                ));

        mockMvc.perform(patch("/api/projects/"+projectId+"/archive")
                .with(user("member@example.com").roles("MEMBER"))
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(projectService).archive(projectId,memberId,false);
    }

    @Test
    @WithMockUser(username = "owner@example.com",roles = "MEMBER")
    void archivedProjectCannotBeArchivedAgain() throws Exception{

        UUID projectId = UUID.randomUUID();
        UUID userId=UUID.randomUUID();

        when(userService.getCurrentUser(anyString()))
                .thenReturn(new UserResponseDTO(
                        userId,
                        "Proje sahibi",
                        "owner@example.com",
                        UserRole.MEMBER,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));

        when(projectService.archive(projectId,userId,false))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Proje zaten arşivlenmiş"
                ));

        mockMvc.perform(patch("/api/projects/"+projectId+"/archive")
                .with(user("owner@example.com").roles("MEMBER"))
                .with(csrf()))
                .andExpect(status().isConflict());

        verify(projectService).archive(projectId,userId,false);
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles="MEMBER")
    void archivedProjectCannotBeUpdated() throws Exception{
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userService.getCurrentUser(anyString()))
                .thenReturn(new UserResponseDTO(
                        userId,
                        "Proje sahibi",
                        "owner@example.com",
                        UserRole.MEMBER,
                        true,
                        Instant.parse("2026-10-26T08:00:00Z")
                ));

        when(projectService.update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(userId),
                eq(false)
        )).thenThrow(new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Arşivlenmiş proje güncellenmez"
        ));
        mockMvc.perform(put("/api/projects/" + projectId)
                .with(user("owner@example.com").roles("MEMBER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": "Güncellenmeye çalışılam proje",
                        "description": "Arşivlenmiş proje"
                        }
                        """))
                .andExpect(status().isConflict());

        verify(projectService).update(
                eq(projectId),
                any(ProjectRequestDto.class),
                eq(userId),
                eq(false)
        );
    }
    }
