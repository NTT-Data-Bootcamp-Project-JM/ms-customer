package com.nttdata.bank.customer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Profile and document combinations required by the project.
 */
class CustomerRulesTest {

    @Test
    void personalVipWithDniIsAccepted() {
        assertDoesNotThrow(() -> CustomerRules.validate(CustomerType.PERSONAL, CustomerProfile.VIP, DocumentType.DNI));
    }

    @Test
    void personalCustomerCannotBePyme() {
        assertThrows(IllegalArgumentException.class,
                () -> CustomerRules.validate(CustomerType.PERSONAL, CustomerProfile.PYME, DocumentType.DNI));
    }

    @Test
    void businessCustomerMustUseRucAndCannotBeVip() {
        assertThrows(IllegalArgumentException.class,
                () -> CustomerRules.validate(CustomerType.BUSINESS, CustomerProfile.VIP, DocumentType.RUC));
        assertThrows(IllegalArgumentException.class,
                () -> CustomerRules.validate(CustomerType.BUSINESS, CustomerProfile.PYME, DocumentType.DNI));
        assertDoesNotThrow(() -> CustomerRules.validate(CustomerType.BUSINESS, CustomerProfile.PYME, DocumentType.RUC));
    }
}
