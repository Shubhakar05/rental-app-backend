package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.LaptopRentalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface LaptopRentalTransactionRepository extends JpaRepository<LaptopRentalTransaction, Long> {

    /**
     * Find all transactions for a given rental order UID.
     */
    List<LaptopRentalTransaction> findByRentalOrder_Uid(String rentalOrderUid);

    /**
     * Find all transactions for a given consumer UID.
     */
    List<LaptopRentalTransaction> findByConsumer_Uid(String consumerUid);

    /**
     * Find all transactions by status (PENDING, PAID, etc.)
     */
    List<LaptopRentalTransaction> findByStatus(String status);

    List<LaptopRentalTransaction> findByRentalOrder_Id(Long rentalOrderId);

    Optional<LaptopRentalTransaction> findByUid(String uid);


    List<LaptopRentalTransaction> findByRentalOrderId(Long id);
}
