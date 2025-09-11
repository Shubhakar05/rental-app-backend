package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.LaptopTransactionDTO;
import com.scaleorange.rentalapp.entitys.LaptopRentalTransaction;
import com.scaleorange.rentalapp.entitys.Laptops;

import com.scaleorange.rentalapp.enums.LaptopTransactionStatusEnum;
import com.scaleorange.rentalapp.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private static final BigDecimal GST_RATE = BigDecimal.valueOf(18); // 18%
    private static final BigDecimal DEPOSIT_MULTIPLIER = BigDecimal.valueOf(3); // 3 months deposit

    @Override
    public LaptopTransactionDTO calculateBillingDTO(List<Laptops> laptops,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate,
                                                    String rentalOrderUid,
                                                    String vendorUid,
                                                    String consumerUid) {

        // 1️⃣ Calculate base amount (prorated by days)
        BigDecimal baseAmount = BigDecimal.ZERO;
        for (Laptops laptop : laptops) {
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // include end day
            BigDecimal monthlyRent = BigDecimal.valueOf(laptop.getPricePerMonth());
            BigDecimal dailyRent = monthlyRent.divide(BigDecimal.valueOf(30), 2, BigDecimal.ROUND_HALF_UP);
            baseAmount = baseAmount.add(dailyRent.multiply(BigDecimal.valueOf(days)));
        }

        // 2️⃣ GST split
        BigDecimal totalGst = baseAmount.multiply(GST_RATE).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal sgst = totalGst.subtract(cgst);

        // 3️⃣ Security deposit (3 months per laptop)
        BigDecimal depositAmount = laptops.stream()
                .map(l -> BigDecimal.valueOf(l.getPricePerMonth()).multiply(DEPOSIT_MULTIPLIER))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4️⃣ Late fee (initially 0, updated later by batch job)
        BigDecimal lateFee = BigDecimal.ZERO;

        // 5️⃣ Total amount
        BigDecimal totalAmount = baseAmount.add(cgst).add(sgst).add(depositAmount).add(lateFee);

        // 6️⃣ Map to DTO
        LaptopTransactionDTO dto = LaptopTransactionDTO.builder()
                .rentalOrderUid(rentalOrderUid)
                .vendorUid(vendorUid)
                .consumerUid(consumerUid)
                .rentalStart(startDate)
                .rentalEnd(endDate)
                .baseAmount(baseAmount)
                .cgst(cgst)
                .sgst(sgst)
                .totalGst(totalGst)
                .depositAmount(depositAmount)
                .lateFee(lateFee)
                .totalAmount(totalAmount)
                .status(LaptopTransactionStatusEnum.PENDING)
                .build();

        return dto;
    }

    public BigDecimal calculateLateFee(LaptopRentalTransaction txn) {
        LocalDateTime end = txn.getActualReturnDate() != null ? txn.getActualReturnDate() : LocalDateTime.now();
        long overdueDays = ChronoUnit.DAYS.between(txn.getRentalEnd(), end);
        if (overdueDays <= 0) return BigDecimal.ZERO;

        BigDecimal dailyRent = txn.getBaseAmount()
                .divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(txn.getRentalStart(), txn.getRentalEnd()) + 1),
                        2, BigDecimal.ROUND_HALF_UP);
        return dailyRent.multiply(BigDecimal.valueOf(overdueDays));
    }

    public void updateLateFee(LaptopRentalTransaction txn) {
        BigDecimal lateFee = calculateLateFee(txn);
        txn.setLateFee(lateFee);
        BigDecimal totalAmount = txn.getBaseAmount()
                .add(txn.getCgst())
                .add(txn.getSgst())
                .add(txn.getDepositAmount())
                .add(lateFee);
        txn.setTotalAmount(totalAmount);
    }

}
