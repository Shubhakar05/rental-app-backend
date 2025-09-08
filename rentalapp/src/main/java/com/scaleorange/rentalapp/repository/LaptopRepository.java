package com.scaleorange.rentalapp.repository;

import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LaptopRepository extends JpaRepository<Laptops, Long> {

    Optional<Laptops> findByUid(String uid);

    List<Laptops> findByStatus(LaptopStatusEnum status);

    List<Laptops> findByBrandAndPricePerMonthAndStatus(String brand, Double pricePerMonth, LaptopStatusEnum status);


    List<Laptops> findAllByUidIn(List<String> uids);

}
