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
package org.springframework.samples.petclinic.customers.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Reads visit data from the visits-service to enrich owner/pet reports. Failures are
 * swallowed and reported as "no visits" so a degraded visits-service never fails the
 * whole report — the same fallback behaviour the api-gateway applies with its circuit
 * breaker.
 */
@Component
public class VisitsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(VisitsServiceClient.class);

    private final RestClient restClient;

    public VisitsServiceClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder
            .baseUrl("http://visits-service/")
            .build();
    }

    /**
     * Fetch all visits for the given pets in a single call. Returns an empty result for
     * an empty request or when the visits-service is unreachable.
     */
    public Visits getVisitsForPets(Collection<Integer> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return Visits.empty();
        }
        try {
            Visits visits = restClient.get()
                .uri("pets/visits?petId={petId}", joinIds(petIds))
                .retrieve()
                .body(Visits.class);
            return visits == null ? Visits.empty() : visits;
        } catch (RuntimeException ex) {
            log.warn("Could not load visits for {} pets, continuing with none: {}",
                petIds.size(), ex.getMessage());
            return Visits.empty();
        }
    }

    private String joinIds(Collection<Integer> petIds) {
        return petIds.stream().map(Object::toString).collect(joining(","));
    }
}
