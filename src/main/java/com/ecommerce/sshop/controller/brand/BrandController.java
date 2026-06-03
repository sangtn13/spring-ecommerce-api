package com.ecommerce.sshop.controller.brand;

import com.ecommerce.sshop.dto.brand.BrandDto;
import com.ecommerce.sshop.model.brand.Brand;
import com.ecommerce.sshop.mapper.BrandMapper;
import com.ecommerce.sshop.request.brands.UpsertBrandRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.response.PagedResponse;
import com.ecommerce.sshop.service.brand.IBrandService;
import com.ecommerce.sshop.util.PageUtil;
import com.ecommerce.sshop.util.StringUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/brands")
public class BrandController {
    private final IBrandService brandService;
    private final BrandMapper brandMapper;

    @GetMapping()
    public ResponseEntity<ApiResponse> getAllBrands(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        String normalizedName = StringUtil.trimToNull(name);
        if (normalizedName != null) {
            Brand brand = brandService.getBrandByName(normalizedName);
            return ResponseEntity.ok(new ApiResponse("Brand retrieved successfully", brandMapper.toDto(brand)));
        }

        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<BrandDto> brandPage = brandService.getAllBrandsWithPaging(pageable).map(brandMapper::toDto);
        PagedResponse<BrandDto> pagedResponse = PagedResponse.of(brandPage);
        return ResponseEntity.ok(new ApiResponse("Brands retrieved successfully", pagedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBrandById(@PathVariable String id) {
        Brand brand = brandService.getBrandById(id);
        return ResponseEntity.ok(new ApiResponse("Brand retrieved successfully", brandMapper.toDto(brand)));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PostMapping()
    public ResponseEntity<ApiResponse> addBrand(@RequestBody UpsertBrandRequest request) {
        Brand createdBrand = brandService.addBrand(request);
        return ResponseEntity.ok(new ApiResponse("Brand added successfully", brandMapper.toDto(createdBrand)));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateBrand(@PathVariable String id, @RequestBody UpsertBrandRequest request) {
        Brand updatedBrand = brandService.updateBrand(request, id);
        return ResponseEntity.ok(new ApiResponse("Brand updated successfully", brandMapper.toDto(updatedBrand)));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Manager')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBrand(@PathVariable String id) {
        brandService.deleteBrandById(id);
        return ResponseEntity.ok(new ApiResponse("Brand deleted successfully", null));
    }
}
