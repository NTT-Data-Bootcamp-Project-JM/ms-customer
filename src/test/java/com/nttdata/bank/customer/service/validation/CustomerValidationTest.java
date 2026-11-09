package com.nttdata.bank.customer.service.validation;

import com.nttdata.bank.customer.domain.model.DocumentType;

import com.nttdata.bank.customer.domain.model.CustomerType;

import com.nttdata.bank.customer.domain.model.CustomerProfile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Profile and document combinations required by the project.
 */
class CustomerValidationTest {

    @Test
    void personalVipWithDniIsAccepted() {
        assertDoesNotThrow(() -> CustomerValidation.validate(CustomerType.PERSONAL, CustomerProfile.VIP, DocumentType.DNI));
    }

    @Test
    void personalCustomerCannotBePyme() {
        assertThrows(IllegalArgumentException.class,
                () -> CustomerValidation.validate(CustomerType.PERSONAL, CustomerProfile.PYME, DocumentType.DNI));
    }

    @Test
    void businessCustomerMustUseRucAndCannotBeVip() {
        assertThrows(IllegalArgumentException.class,
                () -> CustomerValidation.validate(CustomerType.BUSINESS, CustomerProfile.VIP, DocumentType.RUC));
        assertThrows(IllegalArgumentException.class,
                () -> CustomerValidation.validate(CustomerType.BUSINESS, CustomerProfile.PYME, DocumentType.DNI));
        assertDoesNotThrow(() -> CustomerValidation.validate(CustomerType.BUSINESS, CustomerProfile.PYME, DocumentType.RUC));
    }
}
