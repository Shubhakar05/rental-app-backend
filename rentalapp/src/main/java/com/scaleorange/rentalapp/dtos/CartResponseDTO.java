package com.scaleorange.rentalapp.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponseDTO {
    private String cartUid;
    private String userUid;
    private List<String> laptopUids;
}
