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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.samples.petclinic.customers.model.report.ReportDimension;
import org.springframework.samples.petclinic.customers.web.report.ReportRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime report query builder. Rather than a fixed JPQL string, the query is assembled
 * on each call from the {@link ReportRequest} with the JPA Criteria API: the grouping
 * column and the {@code WHERE} predicates depend entirely on which fields the caller
 * supplied. Two queries are built from the same helpers — a grouped aggregate (owner/pet
 * counts per group) and a detail query (pet ids + birth dates) used to derive average pet
 * age and to feed visit enrichment in the service layer.
 *
 * <p>Spring Data wires this in automatically because the class name is the fragment
 * interface name suffixed with {@code Impl}.
 */
class OwnerReportRepositoryFragmentImpl implements OwnerReportRepositoryFragment {

    private static final String UNKNOWN_GROUP = "(unspecified)";

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<PetAggregateRow> aggregate(ReportRequest spec) {
        Map<String, GroupAccumulator> detail = collectDetail(spec);
        Map<String, long[]> counts = runGroupedCounts(spec);

        List<PetAggregateRow> rows = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : counts.entrySet()) {
            String key = entry.getKey();
            long ownerCount = entry.getValue()[0];
            long petCount = entry.getValue()[1];
            GroupAccumulator acc = detail.getOrDefault(key, new GroupAccumulator());
            rows.add(new PetAggregateRow(key, ownerCount, petCount, acc.averageAgeYears(), acc.petIds));
        }
        return rows;
    }

    /**
     * Build and run the grouped aggregate query at runtime:
     * {@code SELECT <groupExpr>, COUNT(DISTINCT owner), COUNT(pet) ... GROUP BY <groupExpr>}.
     */
    private Map<String, long[]> runGroupedCounts(ReportRequest spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<Pet> pet = cq.from(Pet.class);
        Join<Pet, Owner> owner = pet.join("owner");
        Join<Pet, PetType> type = pet.join("type");

        Expression<String> groupExpr = groupingExpression(cb, owner, type, spec.dimension());
        List<Predicate> predicates = buildPredicates(cb, pet, owner, type, spec);

        cq.multiselect(groupExpr, cb.countDistinct(owner.get("id")), cb.count(pet.get("id")));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        cq.groupBy(groupExpr);
        cq.orderBy(cb.asc(groupExpr));

        Map<String, long[]> result = new LinkedHashMap<>();
        for (Tuple t : em.createQuery(cq).getResultList()) {
            String key = normalize(t.get(0, String.class));
            long owners = t.get(1, Long.class);
            long pets = t.get(2, Long.class);
            result.put(key, new long[] { owners, pets });
        }
        return result;
    }

    /**
     * Build and run the detail query (same filters + grouping, no aggregation) to gather
     * each group's pet ids and birth dates, then fold them into per-group accumulators.
     */
    private Map<String, GroupAccumulator> collectDetail(ReportRequest spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<Pet> pet = cq.from(Pet.class);
        Join<Pet, Owner> owner = pet.join("owner");
        Join<Pet, PetType> type = pet.join("type");

        Expression<String> groupExpr = groupingExpression(cb, owner, type, spec.dimension());
        List<Predicate> predicates = buildPredicates(cb, pet, owner, type, spec);

        cq.multiselect(groupExpr, pet.get("id"), pet.get("birthDate"));
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        Map<String, GroupAccumulator> detail = new LinkedHashMap<>();
        for (Tuple t : em.createQuery(cq).getResultList()) {
            String key = normalize(t.get(0, String.class));
            Integer petId = t.get(1, Integer.class);
            Date birthDate = t.get(2, Date.class);
            detail.computeIfAbsent(key, k -> new GroupAccumulator()).add(petId, birthDate);
        }
        return detail;
    }

    /**
     * The grouping expression selected at runtime — this is the column that becomes the
     * {@code GROUP BY} target and the row label.
     */
    private Expression<String> groupingExpression(CriteriaBuilder cb,
                                                   Join<Pet, Owner> owner,
                                                   Join<Pet, PetType> type,
                                                   ReportDimension dimension) {
        return switch (dimension) {
            case REGION -> owner.get("dataResidencyRegion");
            case PET_TYPE -> type.get("name");
            case OWNER -> cb.concat(cb.concat(owner.get("firstName"), " "), owner.get("lastName"));
        };
    }

    /**
     * Assemble the dynamic {@code WHERE} clause: each optional filter contributes a
     * predicate only when the caller supplied it.
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb,
                                            Root<Pet> pet,
                                            Join<Pet, Owner> owner,
                                            Join<Pet, PetType> type,
                                            ReportRequest spec) {
        List<Predicate> predicates = new ArrayList<>();
        if (spec.hasRegions()) {
            predicates.add(owner.get("dataResidencyRegion").in(spec.regions()));
        }
        if (spec.hasPetTypeIds()) {
            predicates.add(type.get("id").in(spec.petTypeIds()));
        }
        if (spec.hasLastNamePrefix()) {
            predicates.add(cb.like(owner.get("lastName"), spec.lastNamePrefix() + "%"));
        }
        if (spec.bornAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(pet.<Date>get("birthDate"), toDate(spec.bornAfter())));
        }
        if (spec.bornBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(pet.<Date>get("birthDate"), toDate(spec.bornBefore())));
        }
        return predicates;
    }

    private static String normalize(String key) {
        return (key == null || key.isBlank()) ? UNKNOWN_GROUP : key;
    }

    private static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date date) {
        // java.sql.Date#toInstant throws, so go via epoch millis to stay type-agnostic.
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Mutable per-group tally of pet ids and ages, used only while transforming rows. */
    private static final class GroupAccumulator {
        private final List<Integer> petIds = new ArrayList<>();
        private long ageYearsSum;
        private int agedPets;

        void add(Integer petId, Date birthDate) {
            if (petId != null) {
                petIds.add(petId);
            }
            if (birthDate != null) {
                ageYearsSum += Period.between(toLocalDate(birthDate), LocalDate.now()).getYears();
                agedPets++;
            }
        }

        double averageAgeYears() {
            return agedPets == 0 ? 0.0 : (double) ageYearsSum / agedPets;
        }
    }
}
