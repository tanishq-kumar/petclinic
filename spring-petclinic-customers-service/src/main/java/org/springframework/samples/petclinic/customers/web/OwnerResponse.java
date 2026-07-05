package org.springframework.samples.petclinic.customers.web;

import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;

import java.util.List;

/**
 * Public API projection — excludes tokenized SSN and passport data.
 */
record OwnerResponse(
    Integer id,
    String firstName,
    String lastName,
    String address,
    String telephone,
    String dataResidencyRegion,
    List<Pet> pets
) {

    static OwnerResponse from(Owner owner) {
        return new OwnerResponse(
            owner.getId(),
            owner.getFirstName(),
            owner.getLastName(),
            owner.getAddress(),
            owner.getTelephone(),
            owner.getDataResidencyRegion(),
            owner.getPets()
        );
    }
}
