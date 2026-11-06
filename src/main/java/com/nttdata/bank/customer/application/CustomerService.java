package com.nttdata.bank.customer.application;

import com.nttdata.bank.customer.api.CustomerDtos.CustomerRequest;
import com.nttdata.bank.customer.api.CustomerDtos.CustomerResponse;
import com.nttdata.bank.customer.config.CustomerCatalog;
import com.nttdata.bank.customer.domain.CustomerDocument;
import com.nttdata.bank.customer.domain.CustomerRepository;
import com.nttdata.bank.customer.domain.CustomerRules;
import com.nttdata.bank.customer.domain.CustomerType;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Customer use cases. Persistence stays inside this service.
 */
@Service
public class CustomerService {

    private final CustomerRepository customers;
    private final CustomerCatalog catalog;

    /**
     * @param customers customer collection
     * @param catalog document types and profiles stored in Redis
     */
    public CustomerService(CustomerRepository customers, CustomerCatalog catalog) {
        this.customers = customers;
        this.catalog = catalog;
    }

    /**
     * Creates a customer after checking the profile and document rules.
     *
     * @param request customer data
     * @return stored customer
     */
    public Single<CustomerResponse> create(CustomerRequest request) {
        return Single.fromCallable(() -> {
            validateNames(request);
            CustomerRules.validate(request.customerType(), request.profile(), request.documentType());
            validateCatalog(request);
            customers.findByDocumentTypeAndDocumentNumber(request.documentType(), request.documentNumber())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("A customer with that document already exists");
                    });
            return toResponse(customers.save(copy(new CustomerDocument(), request)));
        }).subscribeOn(Schedulers.io());
    }

    /**
     * @return every customer
     */
    public Single<List<CustomerResponse>> findAll() {
        return Single.fromCallable(() -> customers.findAll().stream().map(this::toResponse).toList())
                .subscribeOn(Schedulers.io());
    }

    /**
     * @param id customer identifier
     * @return the customer
     */
    public Single<CustomerResponse> findById(String id) {
        return Single.fromCallable(() -> toResponse(load(id))).subscribeOn(Schedulers.io());
    }

    /**
     * Replaces the customer data and runs the same rules as creation.
     *
     * @param id customer identifier
     * @param request new data
     * @return updated customer
     */
    public Single<CustomerResponse> update(String id, CustomerRequest request) {
        return Single.fromCallable(() -> {
            validateNames(request);
            CustomerRules.validate(request.customerType(), request.profile(), request.documentType());
            validateCatalog(request);
            CustomerDocument current = load(id);
            customers.findByDocumentTypeAndDocumentNumber(request.documentType(), request.documentNumber())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("A customer with that document already exists");
                    });
            return toResponse(customers.save(copy(current, request)));
        }).subscribeOn(Schedulers.io());
    }

    /**
     * Deletes a customer. Products in other services keep only this identifier.
     *
     * @param id customer identifier
     * @return completion
     */
    public Single<Boolean> delete(String id) {
        return Single.fromCallable(() -> {
            if (!customers.existsById(id)) {
                throw new IllegalArgumentException("Customer was not found");
            }
            customers.deleteById(id);
            return true;
        }).subscribeOn(Schedulers.io());
    }

    private CustomerDocument load(String id) {
        return customers.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer was not found"));
    }

    private void validateCatalog(CustomerRequest request) {
        if (!catalog.allowsDocument(request.documentType())) {
            throw new IllegalArgumentException("The document type is not in the catalog");
        }
        if (!catalog.allowsProfile(request.profile())) {
            throw new IllegalArgumentException("The profile is not in the catalog");
        }
    }

    private void validateNames(CustomerRequest request) {
        if (request.customerType() == CustomerType.PERSONAL
                && (isBlank(request.firstName()) || isBlank(request.lastName()))) {
            throw new IllegalArgumentException("A personal customer needs a first name and a last name");
        }
        if (request.customerType() == CustomerType.BUSINESS && isBlank(request.businessName())) {
            throw new IllegalArgumentException("A business customer needs a business name");
        }
    }

    private CustomerDocument copy(CustomerDocument target, CustomerRequest request) {
        target.setDocumentType(request.documentType());
        target.setDocumentNumber(request.documentNumber().trim());
        target.setFirstName(request.firstName());
        target.setLastName(request.lastName());
        target.setBusinessName(request.businessName());
        target.setEmail(request.email());
        target.setPhone(request.phone());
        target.setCustomerType(request.customerType());
        target.setProfile(request.profile());
        return target;
    }

    private CustomerResponse toResponse(CustomerDocument customer) {
        return new CustomerResponse(customer.getId(), customer.getDocumentType(), customer.getDocumentNumber(),
                customer.getFirstName(), customer.getLastName(), customer.getBusinessName(), customer.getEmail(),
                customer.getPhone(), customer.getCustomerType(), customer.getProfile());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
