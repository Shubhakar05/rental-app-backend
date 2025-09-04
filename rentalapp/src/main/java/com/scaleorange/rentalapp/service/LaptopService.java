package com.scaleorange.rentalapp.service;

import com.scaleorange.rentalapp.dtos.LaptopRequestDTO;
import com.scaleorange.rentalapp.dtos.LaptopResponseDTO;
import com.scaleorange.rentalapp.enums.LaptopStatusEnum;

import java.util.List;

public interface LaptopService {

    LaptopResponseDTO addLaptop(LaptopRequestDTO request, String vendorUid);

    LaptopResponseDTO updateLaptop(String uid, LaptopRequestDTO request, String vendorUid);

    void deleteLaptop(String uid, String vendorUid);

    LaptopResponseDTO getLaptop(String uid);

    List<LaptopResponseDTO> getAllLaptops();

    List<LaptopResponseDTO> getLaptopsByStatus(LaptopStatusEnum status);
}
