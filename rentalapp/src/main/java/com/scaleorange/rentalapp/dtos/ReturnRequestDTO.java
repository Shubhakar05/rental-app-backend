package com.scaleorange.rentalapp.dtos;

import lombok.Data;

@Data
public class ReturnRequestDTO {
    private boolean depositApproved;      // whether refund is approved
    private boolean isDamaged;            // flag if laptop is damaged
    private String inspectionRemarks;     // notes from inspection
}
