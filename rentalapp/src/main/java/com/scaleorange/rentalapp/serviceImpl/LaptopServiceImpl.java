package com.scaleorange.rentalapp.serviceImpl;

import com.scaleorange.rentalapp.dtos.LaptopRequestDTO;
import com.scaleorange.rentalapp.dtos.LaptopResponseDTO;
import com.scaleorange.rentalapp.entitys.Laptops;
import com.scaleorange.rentalapp.enums.LaptopStatusEnum;
import com.scaleorange.rentalapp.repository.LaptopRepository;
import com.scaleorange.rentalapp.service.CloudinaryService;
import com.scaleorange.rentalapp.service.LaptopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LaptopServiceImpl implements LaptopService {

    private final LaptopRepository laptopRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public LaptopResponseDTO addLaptop(LaptopRequestDTO request, String vendorUid) {
        String imageUrl = null;
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(request.getImageFile());
        }

        Laptops laptop = Laptops.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .pricePerMonth(request.getPricePerMonth())
                .specs(request.getSpecs())
                .imageUrl(imageUrl)
                .vendorUid(vendorUid)
                .status(LaptopStatusEnum.AVAILABLE)
                .build();

        return toDTO(laptopRepository.save(laptop));
    }

    @Override
    public LaptopResponseDTO updateLaptop(String uid, LaptopRequestDTO request, String vendorUid) {
        Laptops laptop = laptopRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Laptop not found"));

        // Vendor can only update their own laptops
        if (!laptop.getVendorUid().equals(vendorUid)) {
            throw new RuntimeException("Unauthorized: This laptop does not belong to you");
        }

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            laptop.setImageUrl(cloudinaryService.uploadFile(request.getImageFile()));
        }

        laptop.setBrand(request.getBrand());
        laptop.setModel(request.getModel());
        laptop.setPricePerMonth(request.getPricePerMonth());
        laptop.setSpecs(request.getSpecs());

        return toDTO(laptopRepository.save(laptop));
    }

    @Override
    public void deleteLaptop(String uid, String vendorUid) {
        Laptops laptop = laptopRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Laptop not found"));

        if (!laptop.getVendorUid().equals(vendorUid)) {
            throw new RuntimeException("Unauthorized: This laptop does not belong to you");
        }

        laptopRepository.delete(laptop);
    }

    @Override
    public LaptopResponseDTO getLaptop(String uid) {
        Laptops laptop = laptopRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Laptop not found"));
        return toDTO(laptop);
    }

    @Override
    public List<LaptopResponseDTO> getAllLaptops() {
        return laptopRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LaptopResponseDTO> getLaptopsByStatus(LaptopStatusEnum status) {
        return laptopRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private LaptopResponseDTO toDTO(Laptops laptop) {
        return LaptopResponseDTO.builder()
                .laptopUuid(laptop.getUid())
                .brand(laptop.getBrand())
                .model(laptop.getModel())
                .pricePerMonth(laptop.getPricePerMonth())
                .specs(laptop.getSpecs())
                .status(laptop.getStatus())
                .build();
    }
}
