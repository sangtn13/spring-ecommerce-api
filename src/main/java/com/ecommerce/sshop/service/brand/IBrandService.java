package com.ecommerce.sshop.service.brand;

import java.util.List;

import com.ecommerce.sshop.model.brand.Brand;
import com.ecommerce.sshop.request.brands.UpsertBrandRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBrandService {
    Brand getBrandById(String id);

    Brand getBrandByName(String name);

    List<Brand> getAllBrands();

    Brand addBrand(UpsertBrandRequest request);

    Brand updateBrand(UpsertBrandRequest request, String id);

    void deleteBrandById(String id);

    Page<Brand> getAllBrandsWithPaging(Pageable pageable);
}
