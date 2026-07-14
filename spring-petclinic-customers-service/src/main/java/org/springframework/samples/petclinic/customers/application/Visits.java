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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Wrapper matching the visits-service {@code GET /pets/visits} response shape
 * ({@code { "items": [ ... ] }}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Visits(List<VisitDetails> items) {

    public Visits {
        items = items == null ? List.of() : items;
    }

    public static Visits empty() {
        return new Visits(List.of());
    }
}
