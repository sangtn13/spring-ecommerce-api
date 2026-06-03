package com.ecommerce.sshop.service.brand;

import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.exception.brand.BrandNotFoundException;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.model.brand.Brand;
import com.ecommerce.sshop.repository.brand.IBrandRepository;
import com.ecommerce.sshop.request.brands.UpsertBrandRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService implements IBrandService {
    private static final String BRAND_NOT_FOUND_MESSAGE = "Brand not found!!";

    private final IBrandRepository brandRepository;

    @Override
    public Brand getBrandById(String id) {
        return brandRepository.findById(id).orElseThrow(() -> new BrandNotFoundException(BRAND_NOT_FOUND_MESSAGE));
    }

    @Override
    public Brand getBrandByName(String name) {
        return Optional.ofNullable(brandRepository.findByName(name))
                .orElseThrow(() -> new BrandNotFoundException(BRAND_NOT_FOUND_MESSAGE));
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    public Brand addBrand(UpsertBrandRequest request) {
        String brandName = request.getName();
        if (brandRepository.existsByName(brandName)) {
            throw new AlreadyExistsException(brandName + " already exists");
        }
        return brandRepository.save(new Brand(brandName));
    }

    @Override
    public Brand updateBrand(UpsertBrandRequest request, String id) {
        String brandName = request.getName();
        if (brandRepository.existsByName(brandName)
                && !getBrandById(id).getName().equals(brandName)) {
            throw new AlreadyExistsException(brandName + " already exists");
        }

        return Optional.ofNullable(getBrandById(id)).map(oldBrand -> {
            oldBrand.setName(brandName);
            return brandRepository.save(oldBrand);
        }).orElseThrow(() -> new BrandNotFoundException(BRAND_NOT_FOUND_MESSAGE));
    }

    @Override
    public void deleteBrandById(String id) {
        brandRepository.findById(id).ifPresentOrElse(brandRepository::delete, () -> {
            throw new BrandNotFoundException(BRAND_NOT_FOUND_MESSAGE);
        });
    }

    @Override
    public Page<Brand> getAllBrandsWithPaging(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }
}
