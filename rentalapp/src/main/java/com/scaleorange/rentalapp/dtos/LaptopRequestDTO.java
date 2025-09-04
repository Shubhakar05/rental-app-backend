package com.scaleorange.rentalapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaptopRequestDTO {

    private String brand;
    private String model;
    private Double pricePerMonth;
    private String specs;
    private MultipartFile imageFile;// can be Cloudinary URL or base64
}
