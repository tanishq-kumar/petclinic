package org.springframework.samples.petclinic.customers.model.report;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.report.ReportRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the runtime Criteria query builder end-to-end against the seeded HSQLDB
 * schema (10 owners / 13 pets), covering each grouping dimension and each filter.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OwnerReportRepositoryTest {

    @Autowired
    OwnerRepository ownerRepository;

    @Test
    void groupsByRegion() {
        List<PetAggregateRow> rows = ownerRepository.aggregate(
            new ReportRequest(ReportDimension.REGION, null, null, null, null, null));

        Map<String, PetAggregateRow> byKey = rows.stream().collect(toMap(PetAggregateRow::groupKey, identity()));
        assertThat(byKey.keySet()).containsExactlyInAnyOrder("UK", "EU", "APAC", "HK");
        assertThat(byKey.get("UK").ownerCount()).isEqualTo(4);
        assertThat(byKey.get("UK").petCount()).isEqualTo(5);
        assertThat(byKey.get("UK").petIds()).hasSize(5);
        assertThat(byKey.get("HK").ownerCount()).isEqualTo(1);
        assertThat(byKey.get("HK").petCount()).isEqualTo(1);
    }

    @Test
    void groupsByPetType() {
        List<PetAggregateRow> rows = ownerRepository.aggregate(
            new ReportRequest(ReportDimension.PET_TYPE, null, null, null, null, null));

        Map<String, PetAggregateRow> byKey = rows.stream().collect(toMap(PetAggregateRow::groupKey, identity()));
        assertThat(byKey.get("cat").petCount()).isEqualTo(4);
        assertThat(byKey.get("cat").ownerCount()).isEqualTo(3);
        assertThat(byKey.get("dog").petCount()).isEqualTo(4);
        assertThat(byKey.get("hamster").petCount()).isEqualTo(1);
    }

    @Test
    void filtersByPetTypeId() {
        List<PetAggregateRow> rows = ownerRepository.aggregate(
            new ReportRequest(ReportDimension.PET_TYPE, null, List.of(1), null, null, null));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).groupKey()).isEqualTo("cat");
        assertThat(rows.get(0).petCount()).isEqualTo(4);
    }

    @Test
    void filtersByRegionAndGroupsByOwner() {
        List<PetAggregateRow> rows = ownerRepository.aggregate(
            new ReportRequest(ReportDimension.OWNER, List.of("HK"), null, null, null, null));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).groupKey()).isEqualTo("Maria Escobito");
        assertThat(rows.get(0).petCount()).isEqualTo(1);
    }

    @Test
    void filtersByBirthDateRange() {
        // Pets born on/before 2010-12-31: ids 1,4,5,6,10,11,12 -> 7 pets.
        List<PetAggregateRow> rows = ownerRepository.aggregate(
            new ReportRequest(ReportDimension.PET_TYPE, null, null, null, null, LocalDate.of(2010, 12, 31)));

        long totalPets = rows.stream().mapToLong(PetAggregateRow::petCount).sum();
        assertThat(totalPets).isEqualTo(7);
    }
}
