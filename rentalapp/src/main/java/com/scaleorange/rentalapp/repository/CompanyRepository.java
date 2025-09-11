package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUid(String uid);
    Optional<Company> findByName(String name);

    // Correct single vendor method
    Optional<Company> findFirstByIsVendorTrueAndIsActiveTrue();
}
