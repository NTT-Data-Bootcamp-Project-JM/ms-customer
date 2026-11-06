package com.nttdata.bank.customer.config;

import com.nttdata.bank.customer.domain.CustomerProfile;
import com.nttdata.bank.customer.domain.DocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;

/**
 * Master data for customers. Document types and profiles are stored in Redis and read back on each check.
 * A Redis outage does not block customer CRUD.
 */
@Configuration
public class CustomerCatalog {

    static final String DOCUMENT_TYPES = "catalog:document-types";
    static final String PROFILES = "catalog:profiles";

    private static final Logger LOG = LoggerFactory.getLogger(CustomerCatalog.class);
    private final StringRedisTemplate redis;

    /**
     * @param redis Redis string operations
     */
    public CustomerCatalog(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Seeds the customer catalogs when Redis is reachable.
     *
     * @return startup runner
     */
    @Bean
    public CommandLineRunner seedCustomerCatalog() {
        return args -> seed();
    }

    /**
     * Writes the document-type and profile lists.
     */
    public void seed() {
        try {
            redis.opsForValue().set(DOCUMENT_TYPES, "DNI,CEX,PASSPORT,RUC");
            redis.opsForValue().set(PROFILES, "STANDARD,VIP,PYME");
        } catch (RuntimeException ex) {
            LOG.warn("Customer catalog was not cached in Redis: {}", ex.getMessage());
        }
    }

    /**
     * @param documentType identity document
     * @return true when the catalog contains it, or when Redis has no list yet
     */
    public boolean allowsDocument(DocumentType documentType) {
        return allows(DOCUMENT_TYPES, documentType == null ? null : documentType.name());
    }

    /**
     * @param profile commercial profile
     * @return true when the catalog contains it, or when Redis has no list yet
     */
    public boolean allowsProfile(CustomerProfile profile) {
        return allows(PROFILES, profile == null ? null : profile.name());
    }

    private boolean allows(String key, String code) {
        if (code == null) {
            return false;
        }
        try {
            String raw = redis.opsForValue().get(key);
            if (raw == null || raw.isBlank()) {
                return true;
            }
            return Arrays.stream(raw.split(",")).map(String::trim).anyMatch(code::equals);
        } catch (RuntimeException ex) {
            LOG.warn("Catalog {} was not read from Redis: {}", key, ex.getMessage());
            return true;
        }
    }
}
