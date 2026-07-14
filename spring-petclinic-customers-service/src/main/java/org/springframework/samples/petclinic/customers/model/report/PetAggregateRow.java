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

import java.util.List;

/**
 * One grouped row produced by the runtime report query, before it is enriched with
 * visit data. {@code petIds} carries the pets that fell into the group so the service
 * layer can fetch their visits from the visits-service.
 *
 * @param groupKey        the grouping label (region name / pet-type name / owner name)
 * @param ownerCount      distinct owners in the group
 * @param petCount        pets in the group
 * @param avgPetAgeYears  mean pet age in years within the group
 * @param petIds          ids of the pets in the group (for visit enrichment)
 */
public record PetAggregateRow(
    String groupKey,
    long ownerCount,
    long petCount,
    double avgPetAgeYears,
    List<Integer> petIds
) {
}
