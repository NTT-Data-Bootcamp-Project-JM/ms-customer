package com.nttdata.bank.customer.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Customer collection. Other microservices must not use this repository.
 */
public interface CustomerRepository extends MongoRepository<CustomerDocument, String> {

    /**
     * @param documentType document kind
     * @param documentNumber document number
     * @return matching customer
     */
    Optional<CustomerDocument> findByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}
