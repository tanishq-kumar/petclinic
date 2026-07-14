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

/**
 * The dimension an owner/pet report is grouped by. Each value selects a different
 * grouping column when the report query is assembled at runtime, so the same query
 * builder can produce a per-region, per-pet-type, or per-owner breakdown.
 */
public enum ReportDimension {

    /** Group by the owner's data-residency region (e.g. UK / EU / APAC / HK). */
    REGION,

    /** Group by the pet's type (cat, dog, ...). */
    PET_TYPE,

    /** Group by the individual owner. */
    OWNER
}
