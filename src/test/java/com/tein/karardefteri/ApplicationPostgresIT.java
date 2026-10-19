package com.tein.karardefteri;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationPostgresIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;

    @Test void bootsWithPostgresAndValidFlywayConfiguration() {
        assertThat(jdbc.queryForObject("select version()", String.class)).startsWith("PostgreSQL");
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().pending()).isEmpty();
    }
}
