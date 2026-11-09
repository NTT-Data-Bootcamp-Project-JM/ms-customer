package com.nttdata.bank.customer.service.validation;

import com.nttdata.bank.customer.domain.model.DocumentType;

import com.nttdata.bank.customer.domain.model.CustomerType;

import com.nttdata.bank.customer.domain.model.CustomerProfile;

/**
 * Profile and document rules that do not depend on Spring.
 */
public final class CustomerValidation {

    private CustomerValidation() {
    }

    /**
     * Rejects a profile that does not belong to the customer type, and a document that does not belong to it.
     *
     * @param type customer type
     * @param profile commercial profile
     * @param documentType identity document
     */
    public static void validate(CustomerType type, CustomerProfile profile, DocumentType documentType) {
        if (type == CustomerType.PERSONAL && profile == CustomerProfile.PYME) {
            throw new IllegalArgumentException("A personal customer cannot have the PYME profile");
        }
        if (type == CustomerType.BUSINESS && profile == CustomerProfile.VIP) {
            throw new IllegalArgumentException("A business customer cannot have the VIP profile");
        }
        if (type == CustomerType.PERSONAL && documentType == DocumentType.RUC) {
            throw new IllegalArgumentException("A personal customer cannot be identified with RUC");
        }
        if (type == CustomerType.BUSINESS && documentType != DocumentType.RUC) {
            throw new IllegalArgumentException("A business customer must be identified with RUC");
        }
    }
}
