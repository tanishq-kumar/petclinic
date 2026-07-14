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

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.samples.petclinic.customers.model.report.ReportDimension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * The report response: the dimension it was grouped by, when it was generated, and one
 * {@link Row} per group with the owner/pet aggregates enriched by visit metrics.
 */
public record OwnerReport(
    ReportDimension dimension,
    Instant generatedAt,
    List<Row> rows
) {

    /**
     * A single grouped row of the report.
     *
     * @param group            grouping label (region / pet-type / owner name)
     * @param owners           distinct owners in the group
     * @param pets             pets in the group
     * @param avgPetAgeYears   mean pet age in years
     * @param totalVisits      total visits recorded for the group's pets
     * @param avgVisitsPerPet  mean visits per pet in the group
     * @param lastVisitDate    most recent visit date for the group, or null if none
     */
    public record Row(
        String group,
        long owners,
        long pets,
        double avgPetAgeYears,
        long totalVisits,
        double avgVisitsPerPet,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate lastVisitDate
    ) {
    }
}
