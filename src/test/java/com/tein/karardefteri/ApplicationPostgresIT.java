package com.tein.karardefteri;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

@SpringBootTest
class ApplicationPostgresIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;

    private UUID integrationTestUserId;
    private UUID integrationTestProjectId;

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

    @AfterEach
    void cleanUp() {
        if (integrationTestProjectId != null){
            jdbc.update(
                    "DELETE FROM projects WHERE id = ?",
                    integrationTestProjectId
            );
            integrationTestProjectId = null;
        }
        if(integrationTestUserId != null){
            jdbc.update(
                    "DELETE FROM users WHERE id = ?",
                    integrationTestUserId
            );
            integrationTestUserId = null;
        }
    }
}
