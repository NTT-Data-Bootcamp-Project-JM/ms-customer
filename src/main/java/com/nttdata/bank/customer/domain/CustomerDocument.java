package com.nttdata.bank.customer.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Customer aggregate stored in the customer database.
 */
@Getter
@Setter
@Document("customers")
@CompoundIndex(name = "document_uk", def = "{'documentType': 1, 'documentNumber': 1}", unique = true)
public class CustomerDocument {

    @Id
    private String id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String businessName;
    private String email;
    private String phone;
    private CustomerType customerType;
    private CustomerProfile profile;
}
