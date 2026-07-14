package org.springframework.samples.petclinic.customers.web.report;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.application.VisitDetails;
import org.springframework.samples.petclinic.customers.application.Visits;
import org.springframework.samples.petclinic.customers.application.VisitsServiceClient;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.report.PetAggregateRow;
import org.springframework.samples.petclinic.customers.model.report.ReportDimension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Verifies the visit-folding transformation and the graceful fallback when the
 * visits-service returns nothing.
 */
class ReportServiceTest {

    private final OwnerRepository ownerRepository = mock(OwnerRepository.class);
    private final VisitsServiceClient visitsServiceClient = mock(VisitsServiceClient.class);
    private final ReportService reportService = new ReportService(ownerRepository, visitsServiceClient);

    private static ReportRequest regionRequest() {
        return new ReportRequest(ReportDimension.REGION, null, null, null, null, null);
    }

    @Test
    void foldsVisitsIntoRows() {
        given(ownerRepository.aggregate(any())).willReturn(List.of(
            new PetAggregateRow("UK", 2, 3, 12.0, List.of(7, 8, 11))));
        given(visitsServiceClient.getVisitsForPets(any())).willReturn(new Visits(List.of(
            new VisitDetails(1, 7, LocalDate.of(2024, 7, 1)),
            new VisitDetails(2, 8, LocalDate.of(2024, 7, 15)),
            new VisitDetails(3, 8, LocalDate.of(2024, 1, 1)))));

        OwnerReport report = reportService.generate(regionRequest());

        assertThat(report.dimension()).isEqualTo(ReportDimension.REGION);
        assertThat(report.rows()).hasSize(1);
        OwnerReport.Row row = report.rows().get(0);
        assertThat(row.group()).isEqualTo("UK");
        assertThat(row.owners()).isEqualTo(2);
        assertThat(row.pets()).isEqualTo(3);
        assertThat(row.totalVisits()).isEqualTo(3);
        assertThat(row.avgVisitsPerPet()).isEqualTo(1.0);
        assertThat(row.lastVisitDate()).isEqualTo(LocalDate.of(2024, 7, 15));
    }

    @Test
    void degradesGracefullyWhenNoVisits() {
        given(ownerRepository.aggregate(any())).willReturn(List.of(
            new PetAggregateRow("UK", 2, 3, 12.0, List.of(7, 8))));
        given(visitsServiceClient.getVisitsForPets(any())).willReturn(Visits.empty());

        OwnerReport report = reportService.generate(regionRequest());

        OwnerReport.Row row = report.rows().get(0);
        assertThat(row.totalVisits()).isZero();
        assertThat(row.avgVisitsPerPet()).isZero();
        assertThat(row.lastVisitDate()).isNull();
    }
}
