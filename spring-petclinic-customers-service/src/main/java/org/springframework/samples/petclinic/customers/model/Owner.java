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
package org.springframework.samples.petclinic.customers.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.core.style.ToStringCreator;

import java.util.*;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@Entity
@Table(name = "owners")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    @NotBlank
    private String firstName;

    @Column(name = "last_name")
    @NotBlank
    private String lastName;

    @Column(name = "address")
    @NotBlank
    private String address;

    @Column(name = "telephone")
    @NotBlank
    @Digits(fraction = 0, integer = 12)
    private String telephone;

    @Column(name = "ssn", length = 256)
    private String ssn;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "kyc_verified_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date kycVerifiedAt;

    @Column(name = "data_residency_region")
    private String dataResidencyRegion;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "owner")
    private Set<Pet> pets;

    protected Set<Pet> getPetsInternal() {
        if (this.pets == null) {
            this.pets = new HashSet<>();
        }
        return this.pets;
    }

    public List<Pet> getPets() {
        final List<Pet> sortedPets = new ArrayList<>(getPetsInternal());
        PropertyComparator.sort(sortedPets, new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedPets);
    }

    public void addPet(Pet pet) {
        getPetsInternal().add(pet);
        pet.setOwner(this);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("id", this.getId())
            .append("lastName", this.getLastName())
            .append("firstName", this.getFirstName())
            .append("dataResidencyRegion", this.dataResidencyRegion)
            .toString();
    }

    public Integer getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getAddress() {
        return this.address;
    }

    public String getTelephone() {
        return this.telephone;
    }

    @JsonIgnore
    public String getSsn() {
        return this.ssn;
    }

    @JsonIgnore
    public String getPassportNumber() {
        return this.passportNumber;
    }

    public Date getKycVerifiedAt() {
        return this.kycVerifiedAt;
    }

    public String getDataResidencyRegion() {
        return this.dataResidencyRegion;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setKycVerifiedAt(Date kycVerifiedAt) {
        this.kycVerifiedAt = kycVerifiedAt;
    }

    public void setDataResidencyRegion(String dataResidencyRegion) {
        this.dataResidencyRegion = dataResidencyRegion;
    }
}
