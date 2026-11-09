package com.nttdata.bank.customer.api.dto;

import com.nttdata.bank.customer.domain.model.CustomerProfile;
import com.nttdata.bank.customer.domain.model.CustomerType;
import com.nttdata.bank.customer.domain.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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