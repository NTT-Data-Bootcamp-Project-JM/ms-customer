package com.nttdata.bank.customer.mapper;

import com.nttdata.bank.customer.api.dto.CustomerRequest;
import com.nttdata.bank.customer.api.dto.CustomerResponse;
import com.nttdata.bank.customer.domain.model.CustomerDocument;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Maps customer domain models and API DTOs.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * @param document customer aggregate
     * @return API response
     */
    CustomerResponse toResponse(CustomerDocument document);

    /**
     * @param documents customer aggregates
     * @return API responses
     */
    List<CustomerResponse> toResponseList(List<CustomerDocument> documents);

    /**
     * Copies request fields onto an existing document. The identifier is preserved.
     *
     * @param request API request
     * @param document target document
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentNumber", expression = "java(request.documentNumber() == null ? null : request.documentNumber().trim())")
    void updateDocument(CustomerRequest request, @MappingTarget CustomerDocument document);

    /**
     * @param request API request
     * @return new document without identifier
     */
    default CustomerDocument toNewDocument(CustomerRequest request) {
        CustomerDocument document = new CustomerDocument();
        updateDocument(request, document);
        return document;
    }
}