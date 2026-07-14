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

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.customers.model.report.ReportDimension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Owner/pet analytics reports. The report query is assembled at runtime from the request
 * spec, then enriched with visit data pulled from the visits-service.
 */
@RestController
@RequestMapping("/owners/reports")
@Timed("petclinic.report")
class ReportResource {

    private static final Logger log = LoggerFactory.getLogger(ReportResource.class);

    private final ReportService reportService;

    ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Full report request via JSON body — supports every filter.
     */
    @PostMapping
    public OwnerReport report(@Valid @RequestBody ReportRequest request) {
        log.info("Generating {} report with filters {}", request.dimension(), request);
        return reportService.generate(request);
    }

    /**
     * Convenience GET for quick demoing from a browser/curl, e.g.
     * {@code /owners/reports?dimension=REGION&petTypeIds=1,2&bornBefore=2011-12-31}.
     */
    @GetMapping
    public OwnerReport report(
        @RequestParam("dimension") ReportDimension dimension,
        @RequestParam(value = "regions", required = false) List<String> regions,
        @RequestParam(value = "petTypeIds", required = false) List<Integer> petTypeIds,
        @RequestParam(value = "lastNamePrefix", required = false) String lastNamePrefix,
        @RequestParam(value = "bornAfter", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bornAfter,
        @RequestParam(value = "bornBefore", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bornBefore) {

        ReportRequest request = new ReportRequest(dimension, regions, petTypeIds, lastNamePrefix, bornAfter, bornBefore);
        log.info("Generating {} report with filters {}", dimension, request);
        return reportService.generate(request);
    }
}
