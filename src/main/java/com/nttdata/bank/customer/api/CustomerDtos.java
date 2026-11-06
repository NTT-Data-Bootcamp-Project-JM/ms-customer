package com.nttdata.bank.customer.api;

import com.nttdata.bank.customer.domain.CustomerProfile;
import com.nttdata.bank.customer.domain.CustomerType;
import com.nttdata.bank.customer.domain.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Customer API bodies.
 */
public final class CustomerDtos {

    private CustomerDtos() {
    }

    /**
     * @param documentType identity document kind
     * @param documentNumber identity document number
     * @param firstName given name, required for a person
     * @param lastName family name, required for a person
     * @param businessName legal name, required for a company
     * @param email contact email
     * @param phone contact phone
     * @param customerType personal or business
     * @param profile standard, VIP or PYME
     */
    public record CustomerRequest(
            @NotNull DocumentType documentType,
            @NotBlank String documentNumber,
            String firstName,
            String lastName,
            String businessName,
            String email,
            String phone,
            @NotNull CustomerType customerType,
            @NotNull CustomerProfile profile
    ) {
    }

    /**
     * @param id generated identifier
     * @param documentType identity document kind
     * @param documentNumber identity document number
     * @param firstName given name
     * @param lastName family name
     * @param businessName legal name
     * @param email contact email
     * @param phone contact phone
     * @param customerType personal or business
     * @param profile commercial profile
     */
    public record CustomerResponse(
            String id,
            DocumentType documentType,
            String documentNumber,
            String firstName,
            String lastName,
            String businessName,
            String email,
            String phone,
            CustomerType customerType,
            CustomerProfile profile
    ) {
    }
}
