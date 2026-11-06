package com.nttdata.bank.customer.api;

import com.nttdata.bank.customer.api.CustomerDtos.CustomerRequest;
import com.nttdata.bank.customer.api.CustomerDtos.CustomerResponse;
import com.nttdata.bank.customer.api.contract.CustomerApi;
import com.nttdata.bank.customer.application.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implements the customer contract generated from {@code openapi/openapi.yaml}.
 */
@RestController
public class CustomerController implements CustomerApi {

    private final CustomerService customers;

    /**
     * @param customers customer use cases
     */
    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CustomerResponse> create(CustomerRequest request) {
        return customers.create(request)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
                .blockingGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<CustomerResponse>> findAll() {
        return customers.findAll().map(ResponseEntity::ok).blockingGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CustomerResponse> findById(String id) {
        return customers.findById(id).map(ResponseEntity::ok).blockingGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CustomerResponse> update(String id, CustomerRequest request) {
        return customers.update(id, request).map(ResponseEntity::ok).blockingGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> delete(String id) {
        return customers.delete(id).map(done -> ResponseEntity.noContent().<Void>build()).blockingGet();
    }
}
