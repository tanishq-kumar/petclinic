package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test that boots the persistence layer so {@code schema.sql} + {@code data.sql}
 * are executed. It guards against schema/seed drift — e.g. a column being added to (or
 * removed from) the {@code visits} table without the positional INSERTs being updated to
 * match, which fails DB initialization at startup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class VisitSeedDataTest {

    @Autowired
    VisitRepository visitRepository;

    @Test
    void seedDataLoadsAndMatchesSchema() {
        assertThat(visitRepository.count()).isEqualTo(20);
    }
}
