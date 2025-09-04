package com.scaleorange.rentalapp.dtos;

import com.scaleorange.rentalapp.enums.RentalStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RentalResponseDTO {
    private String rentalUid;
    private String userUid;
    private List<String> laptopUids;
    private LocalDateTime rentalTime;
    private LocalDateTime returnTime;
    private RentalStatusEnum status;
}
