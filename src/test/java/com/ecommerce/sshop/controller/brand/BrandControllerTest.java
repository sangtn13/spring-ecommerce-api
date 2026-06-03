package com.ecommerce.sshop.controller.brand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ecommerce.sshop.dto.brand.BrandDto;
import com.ecommerce.sshop.mapper.BrandMapper;
import com.ecommerce.sshop.model.brand.Brand;
import com.ecommerce.sshop.request.brands.UpsertBrandRequest;
import com.ecommerce.sshop.response.ApiResponse;
import com.ecommerce.sshop.service.brand.IBrandService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BrandControllerTest {

    @Mock
    private IBrandService brandService;
    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandController brandController;

    private Brand sampleBrand;
    private BrandDto sampleBrandDto;
    private final String brandId = "brand-uuid-123";
    private final String brandName = "Apple";

    @BeforeEach
    void setUp() {
        sampleBrand = new Brand();
        sampleBrand.setId(brandId);
        sampleBrand.setName(brandName);

        sampleBrandDto = new BrandDto();
        sampleBrandDto.setId(brandId);
        sampleBrandDto.setName(brandName);
    }

    @Test
    @DisplayName("Get all brands with pagination successfully")
    void getAllBrands_Success() {
        Page<Brand> page = new PageImpl<>(List.of(sampleBrand));
        when(brandService.getAllBrandsWithPaging(any(Pageable.class))).thenReturn(page);
        when(brandMapper.toDto(sampleBrand)).thenReturn(sampleBrandDto);

        ResponseEntity<ApiResponse> response = brandController.getAllBrands(null, 1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Brands retrieved successfully", response.getBody().getMessage());
        verify(brandService, times(1)).getAllBrandsWithPaging(any(Pageable.class));
    }

    @Test
    @DisplayName("Treat blank brand name as absent and return brand list")
    void getAllBrands_BlankName_Success() {
        Page<Brand> page = new PageImpl<>(List.of(sampleBrand));
        when(brandService.getAllBrandsWithPaging(any(Pageable.class))).thenReturn(page);
        when(brandMapper.toDto(sampleBrand)).thenReturn(sampleBrandDto);

        ResponseEntity<ApiResponse> response = brandController.getAllBrands(" ", 1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(brandService, times(1)).getAllBrandsWithPaging(any(Pageable.class));
    }

    @Test
    @DisplayName("Get brand by name successfully")
    void getAllBrands_ByName_Success() {
        when(brandService.getBrandByName(brandName)).thenReturn(sampleBrand);
        when(brandMapper.toDto(sampleBrand)).thenReturn(sampleBrandDto);

        ResponseEntity<ApiResponse> response = brandController.getAllBrands(brandName, 1, 5, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Brand retrieved successfully", response.getBody().getMessage());
        assertEquals(sampleBrandDto, response.getBody().getData());
    }

    @Test
    @DisplayName("Get brand by id successfully")
    void getBrandById_Success() {
        when(brandService.getBrandById(brandId)).thenReturn(sampleBrand);
        when(brandMapper.toDto(sampleBrand)).thenReturn(sampleBrandDto);

        ResponseEntity<ApiResponse> response = brandController.getBrandById(brandId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Brand retrieved successfully", response.getBody().getMessage());
        assertEquals(sampleBrandDto, response.getBody().getData());
    }

    @Test
    @DisplayName("Add brand successfully")
    void addBrand_Success() {
        UpsertBrandRequest request = new UpsertBrandRequest();
        request.setName(brandName);
        when(brandService.addBrand(request)).thenReturn(sampleBrand);
        when(brandMapper.toDto(sampleBrand)).thenReturn(sampleBrandDto);

        ResponseEntity<ApiResponse> response = brandController.addBrand(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Brand added successfully", response.getBody().getMessage());
        assertEquals(sampleBrandDto, response.getBody().getData());
    }

    @Test
    @DisplayName("Update brand successfully")
    void updateBrand_Success() {
        UpsertBrandRequest request = new UpsertBrandRequest();
        request.setName("New Apple");
        when(brandService.updateBrand(request, brandId)).thenReturn(sampleBrand);
        when(brandMapper.toDto(sampleBrand)).thenReturn(sampleBrandDto);

        ResponseEntity<ApiResponse> response = brandController.updateBrand(brandId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Brand updated successfully", response.getBody().getMessage());
        verify(brandService).updateBrand(request, brandId);
    }

    @Test
    @DisplayName("Delete brand successfully")
    void deleteBrand_Success() {
        doNothing().when(brandService).deleteBrandById(brandId);

        ResponseEntity<ApiResponse> response = brandController.deleteBrand(brandId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Brand deleted successfully", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(brandService).deleteBrandById(brandId);
    }
}
