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
import jakarta.validation.constraints.NotNull;
import org.springframework.samples.petclinic.customers.model.report.ReportDimension;

import java.time.LocalDate;
import java.util.List;

/**
 * Runtime specification for an owner/pet analytics report. Every field except
 * {@code dimension} is an optional filter; the report query is assembled at runtime
 * from whichever filters are actually supplied, so a single builder serves many
 * different reports.
 *
 * @param dimension      how to group the result (required)
 * @param regions        keep only owners in these data-residency regions, if set
 * @param petTypeIds     keep only pets of these types, if set
 * @param lastNamePrefix keep only owners whose last name starts with this, if set
 * @param bornAfter      keep only pets born on/after this date, if set
 * @param bornBefore     keep only pets born on/before this date, if set
 */
public record ReportRequest(

    @NotNull ReportDimension dimension,

    List<String> regions,

    List<Integer> petTypeIds,

    String lastNamePrefix,

    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate bornAfter,

    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate bornBefore
) {

    public boolean hasRegions() {
        return regions != null && !regions.isEmpty();
    }

    public boolean hasPetTypeIds() {
        return petTypeIds != null && !petTypeIds.isEmpty();
    }

    public boolean hasLastNamePrefix() {
        return lastNamePrefix != null && !lastNamePrefix.isBlank();
    }
}
