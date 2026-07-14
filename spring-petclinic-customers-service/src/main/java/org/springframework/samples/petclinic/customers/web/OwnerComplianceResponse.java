package org.springframework.samples.petclinic.customers.web;

import org.springframework.samples.petclinic.customers.model.Owner;

import java.util.Date;

/**
 * Restricted compliance projection for KYC/AML review workflows.
 */
record OwnerComplianceResponse(
    Integer id,
    String firstName,
    String lastName,
    String passportNumber,
    String maskedSsn,
    Date kycVerifiedAt,
    String dataResidencyRegion
) {

    static OwnerComplianceResponse from(Owner owner) {
        return new OwnerComplianceResponse(
            owner.getId(),
            owner.getFirstName(),
            owner.getLastName(),
            owner.getPassportNumber(),
            maskSsn(owner.getSsn()),
            owner.getKycVerifiedAt(),
            owner.getDataResidencyRegion()
        );
    }

    private static String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 4) {
            return null;
        }
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
}
