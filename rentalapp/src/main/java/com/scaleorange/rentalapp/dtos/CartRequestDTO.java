package com.scaleorange.rentalapp.dtos;

import lombok.Data;
import java.util.List;

@Data
public class CartRequestDTO {
    private List<String> laptopUids;
}
