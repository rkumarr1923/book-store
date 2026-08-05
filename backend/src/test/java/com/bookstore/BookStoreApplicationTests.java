package com.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=dev",
        // H2 in-memory DB — no PostgreSQL required in CI
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        // Use create-drop so Hibernate builds the schema from entities (no Flyway yet)
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        // Suppress open-in-view warning
        "spring.jpa.open-in-view=false",
        // Force Hibernate 6 to use VARCHAR DDL for @Enumerated(STRING) columns (H2 compat)
        "spring.jpa.properties.hibernate.type.preferred_enum_jdbc_type=VARCHAR",
        // Disable data.sql loading in tests — seed data contains PostgreSQL-specific syntax
        "spring.sql.init.mode=never"
})
class BookStoreApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts without errors
    }
}
