/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.customers.web.report;

import org.springframework.samples.petclinic.customers.application.VisitDetails;
import org.springframework.samples.petclinic.customers.application.Visits;
import org.springframework.samples.petclinic.customers.application.VisitsServiceClient;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.report.PetAggregateRow;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Orchestrates an owner/pet analytics report: runs the runtime-built aggregate query,
 * fetches the matching visits from the visits-service in one batch, then folds visit
 * metrics into each group. The visit-folding step is the "process owner/pet data as per
 * visits" transformation.
 */
@Service
public class ReportService {

    private final OwnerRepository ownerRepository;
    private final VisitsServiceClient visitsServiceClient;

    public ReportService(OwnerRepository ownerRepository, VisitsServiceClient visitsServiceClient) {
        this.ownerRepository = ownerRepository;
        this.visitsServiceClient = visitsServiceClient;
    }

    public OwnerReport generate(ReportRequest request) {
        List<PetAggregateRow> aggregates = ownerRepository.aggregate(request);

        // One batched call for every pet across every group.
        Set<Integer> allPetIds = aggregates.stream()
            .flatMap(row -> row.petIds().stream())
            .collect(toSet());
        Visits visits = visitsServiceClient.getVisitsForPets(allPetIds);
        Map<Integer, List<VisitDetails>> visitsByPet = visits.items().stream()
            .collect(groupingBy(VisitDetails::petId));

        List<OwnerReport.Row> rows = aggregates.stream()
            .map(row -> enrichWithVisits(row, visitsByPet))
            .toList();

        return new OwnerReport(request.dimension(), Instant.now(), rows);
    }

    /** Fold the visits belonging to a group's pets into visit metrics for that group. */
    private OwnerReport.Row enrichWithVisits(PetAggregateRow row,
                                             Map<Integer, List<VisitDetails>> visitsByPet) {
        long totalVisits = 0;
        LocalDate lastVisitDate = null;
        for (Integer petId : row.petIds()) {
            for (VisitDetails visit : visitsByPet.getOrDefault(petId, List.of())) {
                totalVisits++;
                if (visit.date() != null && (lastVisitDate == null || visit.date().isAfter(lastVisitDate))) {
                    lastVisitDate = visit.date();
                }
            }
        }
        double avgVisitsPerPet = row.petCount() == 0 ? 0.0 : (double) totalVisits / row.petCount();

        return new OwnerReport.Row(
            row.groupKey(),
            row.ownerCount(),
            row.petCount(),
            round(row.avgPetAgeYears()),
            totalVisits,
            round(avgVisitsPerPet),
            lastVisitDate
        );
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
