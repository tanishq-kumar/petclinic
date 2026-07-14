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
package org.springframework.samples.petclinic.customers.model.report;

import org.springframework.samples.petclinic.customers.web.report.ReportRequest;

import java.util.List;

/**
 * Custom Spring Data fragment that builds and runs the owner/pet analytics query at
 * runtime with the JPA Criteria API. It is mixed into {@code OwnerRepository}, so the
 * dynamic reporting query lives alongside the ordinary derived queries.
 */
public interface OwnerReportRepositoryFragment {

    /**
     * Build a Criteria query from the given spec (dynamic filters + grouping + aggregate
     * selections) and return one aggregated row per group.
     */
    List<PetAggregateRow> aggregate(ReportRequest spec);
}
