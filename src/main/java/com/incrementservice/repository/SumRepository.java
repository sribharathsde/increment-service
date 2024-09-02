package com.incrementservice.repository;

import com.incrementservice.entity.SumEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing SumEntity data.
 */
@Repository
public interface SumRepository extends JpaRepository<SumEntity, String> {

    /**
     * Finds a SumEntity by its key.
     *
     * @param key the key to search for
     * @return an Optional containing the found SumEntity, or empty if not found
     */
    Optional<SumEntity> findByKey(String key);
}
