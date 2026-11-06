package com.nttdata.bank.customer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * Caches the master list of document types in Redis, as required for catalog data.
 */
@Configuration
public class DocumentTypeCatalog {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentTypeCatalog.class);
    private final StringRedisTemplate redis;

    /**
     * @param redis Redis string operations
     */
    public DocumentTypeCatalog(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Seeds the catalog when Redis is reachable. A Redis outage does not block customer CRUD.
     *
     * @return startup runner
     */
    @Bean
    public CommandLineRunner seedDocumentTypes() {
        return args -> {
            try {
                redis.opsForValue().set("catalog:document-types", "DNI,CEX,PASSPORT,RUC");
            } catch (RuntimeException ex) {
                LOG.warn("Document type catalog was not cached in Redis: {}", ex.getMessage());
            }
        };
    }
}
