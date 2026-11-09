package com.nttdata.bank.customer.service;

import com.nttdata.bank.customer.api.dto.CustomerRequest;
import com.nttdata.bank.customer.api.dto.CustomerResponse;
import com.nttdata.bank.customer.infrastructure.redis.CustomerCatalog;
import com.nttdata.bank.customer.service.validation.CustomerValidation;
import com.nttdata.bank.customer.domain.model.CustomerDocument;
import com.nttdata.bank.customer.domain.model.CustomerType;
import com.nttdata.bank.customer.domain.repository.CustomerRepository;
import com.nttdata.bank.customer.mapper.CustomerMapper;
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
    private final CustomerMapper mapper;

    /**
     * @param customers customer collection
     * @param catalog document types and profiles stored in Redis
     * @param mapper domain-to-DTO mapper
     */
    public CustomerService(CustomerRepository customers, CustomerCatalog catalog, CustomerMapper mapper) {
        this.customers = customers;
        this.catalog = catalog;
        this.mapper = mapper;
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
            CustomerValidation.validate(request.customerType(), request.profile(), request.documentType());
            validateCatalog(request);
            customers.findByDocumentTypeAndDocumentNumber(request.documentType(), request.documentNumber())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("A customer with that document already exists");
                    });
            return mapper.toResponse(customers.save(mapper.toNewDocument(request)));
        }).subscribeOn(Schedulers.io());
    }

    /**
     * @return every customer
     */
    public Single<List<CustomerResponse>> findAll() {
        return Single.fromCallable(() -> mapper.toResponseList(customers.findAll()))
                .subscribeOn(Schedulers.io());
    }

    /**
     * @param id customer identifier
     * @return the customer
     */
    public Single<CustomerResponse> findById(String id) {
        return Single.fromCallable(() -> mapper.toResponse(load(id))).subscribeOn(Schedulers.io());
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
            CustomerValidation.validate(request.customerType(), request.profile(), request.documentType());
            validateCatalog(request);
            CustomerDocument current = load(id);
            customers.findByDocumentTypeAndDocumentNumber(request.documentType(), request.documentNumber())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("A customer with that document already exists");
                    });
            mapper.updateDocument(request, current);
            return mapper.toResponse(customers.save(current));
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
