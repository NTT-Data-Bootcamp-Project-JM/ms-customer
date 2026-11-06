package com.nttdata.bank.customer.config;

import com.nttdata.bank.customer.domain.CustomerProfile;
import com.nttdata.bank.customer.domain.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Customer master data written to and read from Redis.
 */
@ExtendWith(MockitoExtension.class)
class CustomerCatalogTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> values;

    private CustomerCatalog catalog;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(values);
        catalog = new CustomerCatalog(redis);
    }

    @Test
    void seedWritesDocumentTypesAndProfiles() {
        catalog.seed();

        verify(values).set("catalog:document-types", "DNI,CEX,PASSPORT,RUC");
        verify(values).set("catalog:profiles", "STANDARD,VIP,PYME");
    }

    @Test
    void readsTheStoredLists() {
        when(values.get("catalog:document-types")).thenReturn("DNI,CEX");
        when(values.get("catalog:profiles")).thenReturn("STANDARD,VIP");

        assertTrue(catalog.allowsDocument(DocumentType.DNI));
        assertFalse(catalog.allowsDocument(DocumentType.RUC));
        assertTrue(catalog.allowsProfile(CustomerProfile.VIP));
        assertFalse(catalog.allowsProfile(CustomerProfile.PYME));
    }

    @Test
    void aMissingOrUnavailableCatalogDoesNotBlock() {
        when(values.get("catalog:profiles")).thenReturn(null);
        assertTrue(catalog.allowsProfile(CustomerProfile.PYME));

        when(values.get("catalog:document-types")).thenThrow(new RuntimeException("down"));
        assertTrue(catalog.allowsDocument(DocumentType.PASSPORT));
    }
}
