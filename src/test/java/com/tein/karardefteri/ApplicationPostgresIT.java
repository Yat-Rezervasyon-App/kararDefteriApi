package com.tein.karardefteri;

import com.tein.karardefteri.project.dto.ProjectRequestDto;
import com.tein.karardefteri.project.dto.ProjectResponseDto;
import com.tein.karardefteri.project.service.ProjectService;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.task.service.TaskService;
import com.tein.karardefteri.task.dto.TaskRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

@SpringBootTest
class ApplicationPostgresIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;
    @Autowired ProjectService projectService;
    @Autowired UserService userService;
    @Autowired MeetingService meetingService;
    @Autowired TaskService taskService;

    private UUID integrationTestUserId;
    private UUID integrationTestAdminId;
    private UUID integrationTestProjectId;
    private UUID integrationTestMeetingId;
    private Long integrationTestTaskId;
    private UUID integrationTestOtherUserId;
    private UUID integrationTestOtherProjectId;
    private UUID integrationTestOtherMeetingId;
    private Long integrationTestOtherTaskId;

    @Test void bootsWithPostgresAndValidFlywayConfiguration() {
        assertThat(jdbc.queryForObject("select version()", String.class)).startsWith("PostgreSQL");
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void projectShouldBeStoredWithCorrectOwner() {
        integrationTestUserId = UUID.randomUUID();
        integrationTestProjectId = UUID.randomUUID();

        jdbc.update("""
        INSERT INTO users (
            id, full_name, email, password_hash, role, active
        )
        VALUES (?, ?, ?, ?, ?, ?)
        """,
                integrationTestUserId,
                "Integration Test User",
                "postgres-it-" + integrationTestUserId + "@example.com",
                "test-hash",
                "MEMBER",
                true
        );

        jdbc.update("""
                INSERT INTO projects (
                    id, name, description, created_by, archived, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                integrationTestProjectId,
                "Integration Test Project",
                "PostgreSQL integration test",
                integrationTestUserId,
                false
        );

                UUID savedOwnerId = jdbc.queryForObject(
                        "SELECT created_by FROM projects WHERE id = ?",
                        UUID.class,
                        integrationTestProjectId
                );
                assertThat(savedOwnerId).isEqualTo(integrationTestUserId);
    }

    @Test
    void projectShouldBeCreatedAndUpdatedOnPostgres() {
        integrationTestUserId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (
                id, full_name, email, password_hash, role, active
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                integrationTestUserId,
                "Project Integration User",
                "postgres-it-" + integrationTestUserId + "@example.com",
                "test-hash",
                "MEMBER",
                true
        );
        ProjectResponseDto created = projectService.create(
                new ProjectRequestDto("İlk Proje", "İlk açıklama"),
                integrationTestUserId
        );
        integrationTestProjectId = created.id();

        assertThat(created.name()).isEqualTo("İlk Proje");

        assertThat(created.createdBy()).isEqualTo(integrationTestUserId);

        ProjectResponseDto updated = projectService.update(
                integrationTestProjectId,
                new ProjectRequestDto("Güncel Proje", "Güncel açıklama"),
                integrationTestUserId,
                false
        );

        assertThat(updated.name()).isEqualTo("Güncel Proje");
        assertThat(updated.description()).isEqualTo("Güncel açıklama");
    }

    @Test
    void projectShouldBeArchivedAndFilteredOnPostgres() {
        integrationTestUserId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (
                id, full_name, email, password_hash, role, active
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                integrationTestUserId,
                "Archive Test User",
                "postgres-it-" + integrationTestUserId + "@example.com",
                "test-hash",
                "MEMBER",
                true
        );

        ProjectResponseDto created = projectService.create(
                new ProjectRequestDto("Filtre Projesi","Filtre açıklaması"),
                integrationTestUserId
        );

        integrationTestProjectId = created.id();

        Page<ProjectResponseDto> activeProjects =
                projectService.findAll(
                        "Filtre",
                        false,
                        PageRequest.of(0, 10)
                );
        assertThat(activeProjects.getContent())
                .extracting(ProjectResponseDto::id)
                .contains(integrationTestProjectId);

        ProjectResponseDto archived = projectService.archive(
                integrationTestProjectId,
                integrationTestUserId,
                false
        );

        assertThat(archived.archived()).isTrue();

        Page<ProjectResponseDto> archivedProjects = projectService.findAll(
                "Filtre",
                true,
                PageRequest.of(0,10)
        );

        assertThat(archivedProjects.getContent())
                .extracting(ProjectResponseDto::id)
                .contains(integrationTestProjectId);

        Page<ProjectResponseDto> activeProjectsAfterArchive =
                projectService.findAll(
                        "Filtre",
                        false,
                        PageRequest.of(0, 10)
                );

        assertThat(activeProjectsAfterArchive.getContent())
                .extracting(ProjectResponseDto::id)
                .doesNotContain(integrationTestProjectId);
    }

    @Test
    void projectOwnershipShouldBeEnforcedOnPostgres() {
        integrationTestUserId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO users (
                id, full_name, email, password_hash, role, active
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                integrationTestUserId,
                "Project Owner",
                "postgres-it-" + integrationTestUserId + "@example.com",
                "test-hash",
                "MEMBER",
                true
        );

        ProjectResponseDto created = projectService.create(
                new ProjectRequestDto("Sahiplik Projesi" , "Açıklama"),
                integrationTestUserId
        );

        integrationTestProjectId = created.id();

        UUID anotherUserId = UUID.randomUUID();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> projectService.update(
                        integrationTestProjectId,
                        new ProjectRequestDto("Yetkisiz Güncelleme", "Açıklama"),
                        anotherUserId,
                        false
                )
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        ProjectResponseDto adminUpdated = projectService.update(
                integrationTestProjectId,
                new ProjectRequestDto("Admin Güncellemesi", "Yeni açıklama"),
                anotherUserId,
                true
        );

        assertThat(adminUpdated.name()).isEqualTo("Admin Güncellemesi");
    }

    @Test
    void deactivatingUserShouldPreserveHistoricalRecordsOnPostgres() {
        integrationTestUserId = UUID.randomUUID();
        integrationTestAdminId = UUID.randomUUID();

        // Kullanıcı oluştur
        jdbc.update("""
        INSERT INTO users (
            id, full_name, email, password_hash, role, active
        )
        VALUES (?, ?, ?, ?, ?, ?)
        """,
                integrationTestUserId,
                "Historical Test User",
                "historical-user-" + integrationTestUserId + "@example.com",
                "test-hash",
                "MEMBER",
                true
        );

        // Admin kullanıcı oluştur
        jdbc.update("""
        INSERT INTO users (
            id, full_name, email, password_hash, role, active
        )
        VALUES (?, ?, ?, ?, ?, ?)
        """,
                integrationTestAdminId,
                "Historical Test Admin",
                "historical-admin-" + integrationTestAdminId + "@example.com",
                "test-hash",
                "ADMIN",
                true
        );

        // Proje oluştur
        ProjectResponseDto project = projectService.create(
                new ProjectRequestDto("Historical Project", "History preservation test"),
                integrationTestUserId
        );

        integrationTestProjectId = project.id();

        // Toplantı oluştur
        integrationTestMeetingId = UUID.randomUUID();

        jdbc.update("""
        INSERT INTO meetings (
            id, project_id, title, meeting_at, description,
            notes, created_by, archived
        )
        VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)
        """,
                integrationTestMeetingId,
                integrationTestProjectId,
                "Historical Meeting",
                "Test description",
                "Test notes",
                integrationTestUserId,
                false
        );

        // Görev oluştur
        integrationTestTaskId = jdbc.queryForObject("""
        INSERT INTO tasks (
            meeting_id, title, description, assignee_id
        )
        VALUES (?, ?, ?, ?)
        RETURNING id
        """,
                Long.class,
                integrationTestMeetingId,
                "Historical Task",
                "Test task description",
                integrationTestUserId
        );

        // Kullanıcıyı gerçek servis metodu üzerinden pasifleştir
        userService.updateUserActiveStatus(
                integrationTestUserId,
                integrationTestAdminId
        );

        // Kullanıcının pasif olduğunu doğrula
        Boolean active = jdbc.queryForObject(
                "SELECT active FROM users WHERE id = ?",
                Boolean.class,
                integrationTestUserId
        );

        assertThat(active).isFalse();

        // Proje, toplantı ve görev kayıtlarının korunduğunu doğrula
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE id = ?",
                Integer.class,
                integrationTestProjectId
        )).isEqualTo(1);

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM meetings WHERE id = ?",
                Integer.class,
                integrationTestMeetingId
        )).isEqualTo(1);

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM tasks WHERE id = ?",
                Integer.class,
                integrationTestTaskId
        )).isEqualTo(1);

        // Geçmiş kayıtların kullanıcı ilişkilerinin korunduğunu doğrula
        assertThat(jdbc.queryForObject(
                "SELECT created_by FROM projects WHERE id = ?",
                UUID.class,
                integrationTestProjectId
        )).isEqualTo(integrationTestUserId);

        assertThat(jdbc.queryForObject(
                "SELECT created_by FROM meetings WHERE id = ?",
                UUID.class,
                integrationTestMeetingId
        )).isEqualTo(integrationTestUserId);

        assertThat(jdbc.queryForObject(
                "SELECT assignee_id FROM tasks WHERE id = ?",
                UUID.class,
                integrationTestTaskId
        )).isEqualTo(integrationTestUserId);
    }

    @AfterEach
    void cleanUp() {

        // Buse'nin görevini sil
        if (integrationTestTaskId != null) {
            jdbc.update(
                    "DELETE FROM tasks WHERE id = ?",
                    integrationTestTaskId
            );
            integrationTestTaskId = null;
        }

        // Buse'nin toplantısını sil
        if (integrationTestMeetingId != null) {
            jdbc.update(
                    "DELETE FROM meetings WHERE id = ?",
                    integrationTestMeetingId
            );
            integrationTestMeetingId = null;
        }

        // Kardelen'in görevini sil
        if (integrationTestOtherTaskId != null) {
            jdbc.update(
                    "DELETE FROM tasks WHERE id = ?",
                    integrationTestOtherTaskId
            );
            integrationTestOtherTaskId = null;
        }

        // Kardelen'in toplantısını sil
        if (integrationTestOtherMeetingId != null) {
            jdbc.update(
                    "DELETE FROM meetings WHERE id = ?",
                    integrationTestOtherMeetingId
            );
            integrationTestOtherMeetingId = null;
        }

        // Buse'nin projesini sil
        if (integrationTestProjectId != null) {
            jdbc.update(
                    "DELETE FROM projects WHERE id = ?",
                    integrationTestProjectId
            );
            integrationTestProjectId = null;
        }

        // Kardelen'in projesini sil
        if (integrationTestOtherProjectId != null) {
            jdbc.update(
                    "DELETE FROM projects WHERE id = ?",
                    integrationTestOtherProjectId
            );
            integrationTestOtherProjectId = null;
        }

        // Buse'nin kullanıcısını sil
        if (integrationTestUserId != null) {
            jdbc.update(
                    "DELETE FROM users WHERE id = ?",
                    integrationTestUserId
            );
            integrationTestUserId = null;
        }

        // Admin kullanıcısını sil
        if (integrationTestAdminId != null) {
            jdbc.update(
                    "DELETE FROM users WHERE id = ?",
                    integrationTestAdminId
            );
            integrationTestAdminId = null;
        }

        // Kardelen'in kullanıcısını sil
        if (integrationTestOtherUserId != null) {
            jdbc.update(
                    "DELETE FROM users WHERE id = ?",
                    integrationTestOtherUserId
            );
            integrationTestOtherUserId = null;
        }
    }


    @Test
    void childResourcesShouldEnforceOwnershipAndArchivedProjectRulesOnPostgres() {

        integrationTestUserId = UUID.randomUUID();
        integrationTestOtherUserId = UUID.randomUUID();

        // Buse kullanıcısını oluştur
        jdbc.update("""
            INSERT INTO users (
                id, full_name, email, password_hash, role, active
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                integrationTestUserId,
                "Buse",
                "buse-" + integrationTestUserId + "@example.com",
                "test-hash",
                "MEMBER",
                true
        );

        // Kardelen kullanıcısını oluştur
        jdbc.update("""
            INSERT INTO users (
                id, full_name, email, password_hash, role, active
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
                integrationTestOtherUserId,
                "Kardelen",
                "kardelen-" + integrationTestOtherUserId + "@example.com",
                "test-hash",
                "MEMBER",
                true
        );

        // Buse'nin projesini oluştur
        ProjectResponseDto project = projectService.create(
                new ProjectRequestDto(
                        "Buse'nin Projesi",
                        "Yetki kontrol testi"
                ),
                integrationTestUserId
        );

        integrationTestProjectId = project.id();

        // Kardelen'in projesini oluştur
        ProjectResponseDto otherProject = projectService.create(
                new ProjectRequestDto(
                        "Kardelen'in Projesi",
                        "Kardelen yetki kontrol testi"
                ),
                integrationTestOtherUserId
        );

        integrationTestOtherProjectId = otherProject.id();

        // Buse'nin toplantısını oluştur
        integrationTestMeetingId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO meetings (
                id, project_id, title, meeting_at,
                description, notes, created_by, archived
            )
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)
            """,
                integrationTestMeetingId,
                integrationTestProjectId,
                "Buse'nin Toplantısı",
                "Test açıklaması",
                "Test notları",
                integrationTestUserId,
                false
        );

        // Kardelen'in toplantısını oluştur
        integrationTestOtherMeetingId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO meetings (
                id, project_id, title, meeting_at,
                description, notes, created_by, archived
            )
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)
            """,
                integrationTestOtherMeetingId,
                integrationTestOtherProjectId,
                "Kardelen'in Toplantısı",
                "Test açıklaması",
                "Test notları",
                integrationTestOtherUserId,
                false
        );

        // Buse'nin görevini oluştur
        integrationTestTaskId = jdbc.queryForObject("""
            INSERT INTO tasks (
                meeting_id, title, description, assignee_id
            )
            VALUES (?, ?, ?, ?)
            RETURNING id
            """,
                Long.class,
                integrationTestMeetingId,
                "Buse'nin Görevi",
                "Test görevi",
                integrationTestUserId
        );

        // Kardelen'in görevini oluştur
        integrationTestOtherTaskId = jdbc.queryForObject("""
            INSERT INTO tasks (
                meeting_id, title, description, assignee_id
            )
            VALUES (?, ?, ?, ?)
            RETURNING id
            """,
                Long.class,
                integrationTestOtherMeetingId,
                "Kardelen'in Görevi",
                "Test görevi",
                integrationTestOtherUserId
        );

        // Buse kendi toplantısının görevlerini listeleyebilmeli
        assertThat(
                taskService.getTasksByMeetingId(
                        integrationTestMeetingId,
                        integrationTestUserId,
                        false
                )
        ).hasSize(1);

        // Kardelen kendi toplantısının görevlerini listeleyebilmeli
        assertThat(
                taskService.getTasksByMeetingId(
                        integrationTestOtherMeetingId,
                        integrationTestOtherUserId,
                        false
                )
        ).hasSize(1);

        // Kardelen, Buse'nin toplantısını güncelleyememeli: 403
        ResponseStatusException meetingException = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.updateMeeting(
                        integrationTestMeetingId,
                        "Yetkisiz toplantı güncellemesi",
                        "Açıklama",
                        integrationTestOtherUserId,
                        false
                )
        );

        assertThat(meetingException.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // Kardelen, Buse'nin görevlerini listeleyememeli: 403
        ResponseStatusException taskReadException = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTasksByMeetingId(
                        integrationTestMeetingId,
                        integrationTestOtherUserId,
                        false
                )
        );

        assertThat(taskReadException.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // Kardelen, Buse'nin görevini güncelleyememeli: 403
        ResponseStatusException taskUpdateException = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        integrationTestTaskId,
                        new TaskRequestDto(
                                "Yetkisiz görev güncellemesi",
                                "Açıklama",
                                integrationTestUserId,
                                null,
                                "TODO",
                                "MEDIUM"
                        ),
                        integrationTestOtherUserId,
                        false
                )
        );

        assertThat(taskUpdateException.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // Buse, Kardelen'in toplantısını güncelleyememeli: 403
        ResponseStatusException otherMeetingException = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.updateMeeting(
                        integrationTestOtherMeetingId,
                        "Yetkisiz toplantı güncellemesi",
                        "Açıklama",
                        integrationTestUserId,
                        false
                )
        );

        assertThat(otherMeetingException.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // Buse, Kardelen'in görevlerini listeleyememeli: 403
        ResponseStatusException otherTaskReadException = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTasksByMeetingId(
                        integrationTestOtherMeetingId,
                        integrationTestUserId,
                        false
                )
        );

        assertThat(otherTaskReadException.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // Buse, Kardelen'in görevini güncelleyememeli: 403
        ResponseStatusException otherTaskUpdateException = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        integrationTestOtherTaskId,
                        new TaskRequestDto(
                                "Yetkisiz görev güncellemesi",
                                "Açıklama",
                                integrationTestOtherUserId,
                                null,
                                "TODO",
                                "MEDIUM"
                        ),
                        integrationTestUserId,
                        false
                )
        );

        assertThat(otherTaskUpdateException.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        // Buse kendi projesini arşivlesin
        ProjectResponseDto archivedProject = projectService.archive(
                integrationTestProjectId,
                integrationTestUserId,
                false
        );

        assertThat(archivedProject.archived()).isTrue();

        // Buse'nin arşivlenmiş toplantısı güncellenememeli: 409
        ResponseStatusException archivedMeetingException = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.updateMeeting(
                        integrationTestMeetingId,
                        "Arşivlenmiş toplantı güncellemesi",
                        "Açıklama",
                        integrationTestUserId,
                        false
                )
        );

        assertThat(archivedMeetingException.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // Buse'nin arşivlenmiş görevi güncellenememeli: 409
        ResponseStatusException archivedTaskException = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        integrationTestTaskId,
                        new TaskRequestDto(
                                "Arşivlenmiş görev güncellemesi",
                                "Açıklama",
                                integrationTestUserId,
                                null,
                                "TODO",
                                "MEDIUM"
                        ),
                        integrationTestUserId,
                        false
                )
        );

        assertThat(archivedTaskException.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // Arşivlenmiş projedeki görevin durumu güncellenememeli: 409
        ResponseStatusException archivedTaskStatusException = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(
                        integrationTestTaskId,
                        "DONE",
                        integrationTestUserId
                )
        );

        assertThat(archivedTaskStatusException.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // Kardelen kendi projesini arşivlesin
        ProjectResponseDto archivedOtherProject = projectService.archive(
                integrationTestOtherProjectId,
                integrationTestOtherUserId,
                false
        );

        assertThat(archivedOtherProject.archived()).isTrue();

        // Kardelen'in arşivlenmiş toplantısı güncellenememeli: 409
        ResponseStatusException archivedOtherMeetingException = assertThrows(
                ResponseStatusException.class,
                () -> meetingService.updateMeeting(
                        integrationTestOtherMeetingId,
                        "Arşivlenmiş toplantı güncellemesi",
                        "Açıklama",
                        integrationTestOtherUserId,
                        false
                )
        );

        assertThat(archivedOtherMeetingException.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        // Kardelen'in arşivlenmiş görevi güncellenememeli: 409
        ResponseStatusException archivedOtherTaskException = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        integrationTestOtherTaskId,
                        new TaskRequestDto(
                                "Arşivlenmiş görev güncellemesi",
                                "Açıklama",
                                integrationTestOtherUserId,
                                null,
                                "TODO",
                                "MEDIUM"
                        ),
                        integrationTestOtherUserId,
                        false
                )
        );

        assertThat(archivedOtherTaskException.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

}