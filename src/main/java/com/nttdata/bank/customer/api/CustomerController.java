package com.nttdata.bank.customer.api;

import com.nttdata.bank.customer.api.CustomerDtos.CustomerRequest;
import com.nttdata.bank.customer.api.CustomerDtos.CustomerResponse;
import com.nttdata.bank.customer.application.CustomerService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD API for bank customers.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customers;

    /**
     * @param customers customer use cases
     */
    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    /**
     * @param request customer data
     * @return created customer
     */
    @PostMapping
    public Single<ResponseEntity<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return customers.create(request).map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    /**
     * @return all customers
     */
    @GetMapping
    public Single<ResponseEntity<List<CustomerResponse>>> findAll() {
        return customers.findAll().map(ResponseEntity::ok);
    }

    /**
     * @param id customer identifier
     * @return one customer
     */
    @GetMapping("/{id}")
    public Single<ResponseEntity<CustomerResponse>> findById(@PathVariable String id) {
        return customers.findById(id).map(ResponseEntity::ok);
    }

    /**
     * @param id customer identifier
     * @param request replacement data
     * @return updated customer
     */
    @PutMapping("/{id}")
    public Single<ResponseEntity<CustomerResponse>> update(@PathVariable String id,
                                                           @Valid @RequestBody CustomerRequest request) {
        return customers.update(id, request).map(ResponseEntity::ok);
    }

    /**
     * @param id customer identifier
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public Single<ResponseEntity<Void>> delete(@PathVariable String id) {
        return customers.delete(id).map(done -> ResponseEntity.noContent().build());
    }
}
