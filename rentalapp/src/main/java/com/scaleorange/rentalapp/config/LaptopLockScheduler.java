package com.scaleorange.rentalapp.config;

import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import com.scaleorange.rentalapp.repository.LaptopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LaptopLockScheduler {

    private final LaptopRepository laptopRepository;

    /**
     * Runs every 1 minute to release expired locks
     */
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredLocks() {
        List<Laptops> lockedLaptops = laptopRepository.findByStatus(LaptopStatusEnum.LOCKED);

        for (Laptops laptop : lockedLaptops) {
            if (laptop.isLockExpired()) {
                laptop.setStatus(LaptopStatusEnum.AVAILABLE);
                laptop.setLockTime(null);
                laptopRepository.save(laptop);
                System.out.println("Released expired lock for laptop UID: " + laptop.getUid());
            }
        }
    }
}
