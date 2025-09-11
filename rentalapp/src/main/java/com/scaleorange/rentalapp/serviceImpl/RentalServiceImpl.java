package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.RentalRequestDTO;
import com.scaleorange.rentalapp.dtos.RentalResponseDTO;
import com.scaleorange.rentalapp.dtos.LaptopTransactionDTO;
import com.scaleorange.rentalapp.dtos.ReturnRequestDTO;
import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.entitys.RentalOrder;
import com.scaleorange.rentalapp.entitys.Users;
import com.scaleorange.rentalapp.entitys.LaptopRentalTransaction;
import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import com.scaleorange.rentalapp.enums.LaptopTransactionStatusEnum;
import com.scaleorange.rentalapp.enums.RentalStatusEnum;
import com.scaleorange.rentalapp.repository.LaptopRepository;
import com.scaleorange.rentalapp.repository.RentalOrderRepository;
import com.scaleorange.rentalapp.repository.UsersRepository;
import com.scaleorange.rentalapp.repository.LaptopRentalTransactionRepository;
import com.scaleorange.rentalapp.service.BillingService;
import com.scaleorange.rentalapp.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalOrderRepository rentalRepository;
    private final LaptopRepository laptopRepository;
    private final UsersRepository usersRepository;
    private final LaptopRentalTransactionRepository transactionRepository;
    private final BillingService billingService;

    // Get authenticated user
    private Users getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    // --- Single-laptop checkout ---
    @Override
    public RentalResponseDTO createRental(RentalRequestDTO request) {
        Users consumer = getAuthenticatedUser();

        // Fetch single laptop
        String laptopUid = request.getLaptopUids().get(0);
        Laptops laptop = laptopRepository.findByUid(laptopUid)
                .orElseThrow(() -> new RuntimeException("Laptop not found: " + laptopUid));

        if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
            throw new RuntimeException("Laptop not available: " + laptopUid);
        }

        return processRental(consumer, List.of(laptop), request.getRentalTime(), request.getNumberOfMonths());
    }

    // --- Multi-laptop / cart checkout ---
    @Override
    public RentalResponseDTO createRentalForCart(List<Laptops> laptops, long numberOfMonths,
                                                 LocalDateTime rentalTime, LocalDateTime returnTime) {
        Users consumer = getAuthenticatedUser();

        if (laptops.isEmpty()) {
            throw new RuntimeException("No laptops provided in the cart");
        }

        // Validate availability
        laptops.forEach(laptop -> {
            if (laptop.getStatus() != LaptopStatusEnum.AVAILABLE) {
                throw new RuntimeException("Laptop not available: " + laptop.getUid());
            }
        });

        return processRental(consumer, laptops, rentalTime, numberOfMonths);
    }

    // --- Internal method to handle rental processing ---
    private RentalResponseDTO processRental(Users consumer,
                                            List<Laptops> laptops,
                                            LocalDateTime rentalTime,
                                            long numberOfMonths) {
        if (rentalTime == null) rentalTime = LocalDateTime.now();
        LocalDateTime returnTime = rentalTime.plusMonths(numberOfMonths);

        // 1️⃣ Calculate billing amounts
        LaptopTransactionDTO txnDto = billingService.calculateBillingDTO(
                laptops,
                rentalTime,
                returnTime,
                null, // rental UID not generated yet
                null,
                consumer.getUid()
        );

        // 2️⃣ Create RentalOrder with PENDING status
        RentalOrder rental = RentalOrder.builder()
                .user(consumer)
                .laptops(laptops)
                .status(RentalStatusEnum.PENDING) // keep pending until payment succeeds
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .numberOfMonths(numberOfMonths)
                .baseAmount(txnDto.getBaseAmount())
                .totalGst(txnDto.getCgst().add(txnDto.getSgst()))
                .totalAmount(txnDto.getTotalAmount())
                .build();

        rentalRepository.save(rental);

        // 3️⃣ Generate UID after save if needed
        if (rental.getUid() == null) {
            rental.setUid(rental.generateUid());
            rentalRepository.save(rental);
        }

        // 4️⃣ Create LaptopRentalTransaction for each laptop with PENDING status
        for (Laptops laptop : laptops) {
            String vendorUid = laptop.getVendorUid();
            Users vendor = usersRepository.findByEmail(vendorUid)
                    .orElseThrow(() -> new RuntimeException("Vendor not found: " + vendorUid));

            LaptopRentalTransaction txnEntity = LaptopRentalTransaction.builder()
                    .rentalOrder(rental)
                    .laptop(laptop)
                    .vendor(vendor)
                    .consumer(consumer)
                    .rentalStart(rentalTime)
                    .rentalEnd(returnTime)
                    .baseAmount(txnDto.getBaseAmount())
                    .cgst(txnDto.getCgst())
                    .sgst(txnDto.getSgst())
                    .totalAmount(txnDto.getTotalAmount())
                    .depositAmount(txnDto.getDepositAmount())
                    .lateFee(txnDto.getLateFee())
                    .status(LaptopTransactionStatusEnum.PENDING) // wait for payment
                    .build();

            transactionRepository.save(txnEntity);
        }

        // ✅ 5️⃣ Return response DTO without changing laptop status
        return RentalResponseDTO.builder()
                .rentalUid(rental.getUid())
                .userUid(consumer.getUid())
                .laptopUids(laptops.stream().map(Laptops::getUid).toList())
                .brand(laptops.get(0).getBrand())
                .status(rental.getStatus())
                .rentalTime(rentalTime)
                .returnTime(returnTime)
                .numberOfMonths(numberOfMonths)
                .baseAmount(txnDto.getBaseAmount())
                .totalGst(txnDto.getCgst().add(txnDto.getSgst()))
                .totalAmount(txnDto.getTotalAmount())
                .build();
    }

    @Override
    public LaptopRentalTransaction getTransactionByUid(String uid) {
        return transactionRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Transaction not found for uid: " + uid));
    }


    // Mark rental as paid
    @Override
    public void markRentalAsPaid(RentalOrder rental) {
        rental.setStatus(RentalStatusEnum.ACTIVE);
        rentalRepository.save(rental);

        // Mark laptops as RENTED
        rental.getLaptops().forEach(laptop -> {
            laptop.setStatus(LaptopStatusEnum.RENTED);
            laptopRepository.save(laptop);
        });

        // Mark all linked transactions as RENTED
        List<LaptopRentalTransaction> transactions =
                transactionRepository.findByRentalOrder_Id(rental.getId());
        for (LaptopRentalTransaction txn : transactions) {
            txn.setStatus(com.scaleorange.rentalapp.enums.LaptopTransactionStatusEnum.RENTED);
            transactionRepository.save(txn);
        }
    }

    //return
    @Override
    @Transactional
    public LaptopTransactionDTO returnLaptop(String txnUid, ReturnRequestDTO request) {
        // 1️⃣ Fetch transaction
        LaptopRentalTransaction txn = transactionRepository.findByUid(txnUid)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 2️⃣ Set actual return date
        LocalDateTime now = LocalDateTime.now();
        txn.setActualReturnDate(now.isAfter(txn.getRentalEnd()) ? now : txn.getRentalEnd());

        // 3️⃣ Calculate late fee
        billingService.updateLateFee(txn); // updates txn.setLateFee(...)

        // 4️⃣ Update transaction status
        txn.setStatus(LaptopTransactionStatusEnum.RETURNED);

        // 5️⃣ Handle damage
        txn.setIsDamaged(request.isDamaged());
        txn.setInspectionRemarks(request.getInspectionRemarks());
        if (request.isDamaged()) {
            txn.setDamageCost(txn.getBaseAmount().multiply(BigDecimal.valueOf(0.5))); // 50% of base
            txn.setExtraChargeAmount(BigDecimal.ZERO); // add extra if needed
        } else {
            txn.setDamageCost(BigDecimal.ZERO);
            txn.setExtraChargeAmount(BigDecimal.ZERO);
        }

        // 6️⃣ Handle deposit refund
        if (request.isDepositApproved()) {
            txn.setDepositRefunded(true);
            txn.setDepositRefundAmount(txn.getDepositAmount());
            txn.setRefundDate(LocalDateTime.now());
            txn.setRefundId(UUID.randomUUID().toString());
            txn.setPaymentId(UUID.randomUUID().toString());
        }

        // 7️⃣ Invoice flag
        txn.setInvoiceGenerated(false); // or true if invoice is generated immediately

        // 8️⃣ Persist transaction
        transactionRepository.save(txn);

        // 9️⃣ Update laptop availability
        Laptops laptop = txn.getLaptop();
        laptop.setStatus(LaptopStatusEnum.AVAILABLE);
        laptopRepository.save(laptop);

        // 🔟 Update parent rental order if all laptops returned
        RentalOrder order = txn.getRentalOrder();
        boolean allReturned = order.getTransactions()
                .stream()
                .allMatch(t -> t.getStatus() == LaptopTransactionStatusEnum.RETURNED);
        if (allReturned) {
            order.setStatus(RentalStatusEnum.COMPLETED);
            rentalRepository.save(order);
        }

        // 1️⃣1️⃣ Map entity to DTO using builder
        return LaptopTransactionDTO.builder()
                .uid(txn.getUid())
                .laptopUid(txn.getLaptop().getUid())
                .rentalOrderUid(txn.getRentalOrder().getUid())
                .status(txn.getStatus())
                .actualReturnDate(txn.getActualReturnDate())
                .lateFee(txn.getLateFee())
                .depositRefunded(txn.getDepositRefunded())
                .depositRefundAmount(txn.getDepositRefundAmount())
                .refundDate(txn.getRefundDate())
                .refundId(txn.getRefundId())
                .paymentId(txn.getPaymentId())
                .isDamaged(txn.getIsDamaged())
                .damageCost(txn.getDamageCost())
                .extraChargeAmount(txn.getExtraChargeAmount())
                .inspectionRemarks(txn.getInspectionRemarks())
                .invoiceGenerated(txn.getInvoiceGenerated())
                .baseAmount(txn.getBaseAmount())
                .cgst(txn.getCgst())
                .sgst(txn.getSgst())
                .totalAmount(txn.getTotalAmount())
                .build();
    }




        // Handle failed rental
    @Override
    public void handleFailedRental(RentalOrder rental) {
        rental.setStatus(RentalStatusEnum.FAILED);
        rentalRepository.save(rental);

        // Rollback laptops to AVAILABLE
        rental.getLaptops().forEach(laptop -> {
            laptop.setStatus(LaptopStatusEnum.AVAILABLE);
            laptopRepository.save(laptop);
        });

        // Mark all linked transactions as FAILED
        List<LaptopRentalTransaction> transactions =
                transactionRepository.findByRentalOrder_Id(rental.getId());
        for (LaptopRentalTransaction txn : transactions) {
            txn.setStatus(com.scaleorange.rentalapp.enums.LaptopTransactionStatusEnum.FAILED);
            transactionRepository.save(txn);
        }
    }

}
