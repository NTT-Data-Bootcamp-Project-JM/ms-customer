package com.nttdata.bank.customer.application;

import com.nttdata.bank.customer.api.CustomerDtos.CustomerRequest;
import com.nttdata.bank.customer.api.CustomerDtos.CustomerResponse;
import com.nttdata.bank.customer.config.CustomerCatalog;
import com.nttdata.bank.customer.domain.CustomerDocument;
import com.nttdata.bank.customer.domain.CustomerProfile;
import com.nttdata.bank.customer.domain.CustomerRepository;
import com.nttdata.bank.customer.domain.CustomerType;
import com.nttdata.bank.customer.domain.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Customer use cases with an in-memory stand-in for the repository.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customers;

    @Mock
    private CustomerCatalog catalog;

    private final Map<String, CustomerDocument> store = new LinkedHashMap<>();
    private final AtomicInteger ids = new AtomicInteger();
    private CustomerService service;

    @BeforeEach
    void setUp() {
        store.clear();
        when(catalog.allowsDocument(any())).thenReturn(true);
        when(catalog.allowsProfile(any())).thenReturn(true);
        service = new CustomerService(customers, catalog);
        when(customers.save(any())).thenAnswer(invocation -> {
            CustomerDocument customer = invocation.getArgument(0);
            if (customer.getId() == null) {
                customer.setId("cus-" + ids.incrementAndGet());
            }
            store.put(customer.getId(), customer);
            return customer;
        });
        when(customers.findById(any())).thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(customers.findAll()).thenAnswer(invocation -> new ArrayList<>(store.values()));
        when(customers.existsById(any())).thenAnswer(invocation -> store.containsKey(invocation.getArgument(0)));
        when(customers.findByDocumentTypeAndDocumentNumber(any(), any())).thenAnswer(invocation -> store.values().stream()
                .filter(customer -> customer.getDocumentType() == invocation.getArgument(0)
                        && invocation.getArgument(1).equals(customer.getDocumentNumber()))
                .findFirst());
        doAnswer(invocation -> {
            store.remove(invocation.getArgument(0));
            return null;
        }).when(customers).deleteById(any());
    }

    @Test
    void createFindUpdateAndDeleteAPersonalCustomer() {
        CustomerResponse created = service.create(person("12345678")).blockingGet();

        assertEquals("Ana", created.firstName());
        assertEquals(created.id(), service.findById(created.id()).blockingGet().id());
        assertEquals(1, service.findAll().blockingGet().size());

        CustomerResponse updated = service.update(created.id(),
                new CustomerRequest(DocumentType.DNI, "12345678", "Ana", "Lopez", null, "ana@bank.test", "999",
                        CustomerType.PERSONAL, CustomerProfile.VIP)).blockingGet();

        assertEquals(CustomerProfile.VIP, updated.profile());
        assertTrue(service.delete(created.id()).blockingGet());
        assertThrows(IllegalArgumentException.class, () -> service.findById(created.id()).blockingGet());
    }

    @Test
    void createRejectsAPersonalCustomerWithoutAName() {
        CustomerRequest request = new CustomerRequest(DocumentType.DNI, "12345678", " ", null, null,
                "ana@bank.test", "999", CustomerType.PERSONAL, CustomerProfile.STANDARD);

        assertThrows(IllegalArgumentException.class, () -> service.create(request).blockingGet());
    }

    @Test
    void createRejectsADuplicateDocument() {
        service.create(person("12345678")).blockingGet();

        assertThrows(IllegalArgumentException.class, () -> service.create(person("12345678")).blockingGet());
    }

    @Test
    void updateRejectsADocumentOwnedBySomeoneElse() {
        CustomerResponse first = service.create(person("111")).blockingGet();
        service.create(new CustomerRequest(DocumentType.DNI, "222", "Luis", "Perez", null, "luis@bank.test", "888",
                CustomerType.PERSONAL, CustomerProfile.STANDARD)).blockingGet();

        assertThrows(IllegalArgumentException.class,
                () -> service.update(first.id(), new CustomerRequest(DocumentType.DNI, "222", "Ana", "Perez", null,
                        "ana@bank.test", "999", CustomerType.PERSONAL, CustomerProfile.STANDARD)).blockingGet());
    }

    @Test
    void createStoresABusinessCustomer() {
        CustomerResponse created = service.create(new CustomerRequest(DocumentType.RUC, "20123456789", null, null,
                "NTT Data", "co@bank.test", "111", CustomerType.BUSINESS, CustomerProfile.PYME)).blockingGet();

        assertEquals("NTT Data", created.businessName());
    }

    @Test
    void createRejectsAProfileMissingFromTheCatalog() {
        when(catalog.allowsProfile(any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.create(person("12345678")).blockingGet());
    }

    @Test
    void deleteRejectsAMissingCustomer() {
        assertThrows(IllegalArgumentException.class, () -> service.delete("missing").blockingGet());
    }

    private CustomerRequest person(String documentNumber) {
        return new CustomerRequest(DocumentType.DNI, documentNumber, "Ana", "Perez", null, "ana@bank.test", "999",
                CustomerType.PERSONAL, CustomerProfile.STANDARD);
    }
}
