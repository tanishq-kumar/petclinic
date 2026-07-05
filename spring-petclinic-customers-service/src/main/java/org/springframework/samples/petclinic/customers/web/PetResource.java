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
package org.springframework.samples.petclinic.customers.web;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@RestController
@Timed("petclinic.pet")
class PetResource {

    private static final Logger log = LoggerFactory.getLogger(PetResource.class);

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    PetResource(PetRepository petRepository, OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
    }

    @GetMapping("/petTypes")
    public List<PetType> getPetTypes() {
        return petRepository.findPetTypes();
    }

    @PostMapping("/owners/{ownerId}/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public Pet processCreationForm(
        @Valid @RequestBody PetRequest petRequest,
        @PathVariable("ownerId") @Min(1) int ownerId) {

        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner " + ownerId + " not found"));

        final Pet pet = new Pet();
        owner.addPet(pet);
        return save(pet, petRequest);
    }

    @PutMapping("/owners/{ownerId}/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processUpdateForm(
        @PathVariable("ownerId") @Min(1) int ownerId,
        @PathVariable("petId") @Min(1) int petId,
        @Valid @RequestBody PetRequest petRequest) {
        Pet pet = findPetById(petId);
        assertPetBelongsToOwner(pet, ownerId);
        save(pet, petRequest);
    }

    private Pet save(final Pet pet, final PetRequest petRequest) {
        pet.setName(petRequest.name());
        pet.setBirthDate(petRequest.birthDate());

        PetType type = petRepository.findPetTypeById(petRequest.typeId())
            .orElseThrow(() -> new BadRequestException("Unknown pet type id: " + petRequest.typeId()));
        pet.setType(type);

        log.info("Persisting pet id={} ownerId={}", pet.getId(), pet.getOwner() != null ? pet.getOwner().getId() : null);
        return petRepository.save(pet);
    }

    @GetMapping("owners/*/pets/{petId}")
    public PetDetails findPet(@PathVariable("petId") int petId) {
        Pet pet = findPetById(petId);
        return new PetDetails(pet);
    }

    private Pet findPetById(int petId) {
        return petRepository.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet " + petId + " not found"));
    }

    private void assertPetBelongsToOwner(Pet pet, int ownerId) {
        if (pet.getOwner() == null || !ownerIdEquals(pet.getOwner().getId(), ownerId)) {
            throw new BadRequestException("Pet " + pet.getId() + " does not belong to owner " + ownerId);
        }
    }

    private boolean ownerIdEquals(Integer petOwnerId, int ownerId) {
        return petOwnerId != null && petOwnerId == ownerId;
    }
}
