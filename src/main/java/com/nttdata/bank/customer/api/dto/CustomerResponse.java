package com.nttdata.bank.customer.api.dto;

import com.nttdata.bank.customer.domain.model.CustomerProfile;
import com.nttdata.bank.customer.domain.model.CustomerType;
import com.nttdata.bank.customer.domain.model.DocumentType;

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