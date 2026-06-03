package com.ecommerce.sshop.service.brand;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.ecommerce.sshop.exception.brand.BrandNotFoundException;
import com.ecommerce.sshop.exception.common.AlreadyExistsException;
import com.ecommerce.sshop.model.brand.Brand;
import com.ecommerce.sshop.repository.brand.IBrandRepository;
import com.ecommerce.sshop.request.brands.UpsertBrandRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private IBrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    private Brand sampleBrand;
    private final String brandId = "brand-uuid-123";
    private final String brandName = "Apple";

    @BeforeEach
    void setUp() {
        sampleBrand = new Brand();
        sampleBrand.setId(brandId);
        sampleBrand.setName(brandName);
    }

    @Test
    @DisplayName("Find brand by id successfully")
    void getBrandById_Success() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));

        Brand result = brandService.getBrandById(brandId);

        assertNotNull(result);
        assertEquals(brandId, result.getId());
        verify(brandRepository).findById(brandId);
    }

    @Test
    @DisplayName("Throw when brand id does not exist")
    void getBrandById_NotFound() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        BrandNotFoundException exception = assertThrows(BrandNotFoundException.class,
                () -> brandService.getBrandById(brandId));

        assertEquals("Brand not found!!", exception.getMessage());
    }

    @Test
    @DisplayName("Find brand by name successfully")
    void getBrandByName_Success() {
        when(brandRepository.findByName(brandName)).thenReturn(sampleBrand);

        Brand result = brandService.getBrandByName(brandName);

        assertNotNull(result);
        assertEquals(brandName, result.getName());
        verify(brandRepository).findByName(brandName);
    }

    @Test
    @DisplayName("Throw when brand name does not exist")
    void getBrandByName_NotFound() {
        when(brandRepository.findByName(brandName)).thenReturn(null);

        BrandNotFoundException exception = assertThrows(BrandNotFoundException.class,
                () -> brandService.getBrandByName(brandName));

        assertEquals("Brand not found!!", exception.getMessage());
    }

    @Test
    @DisplayName("Get all brands successfully")
    void getAllBrands_Success() {
        when(brandRepository.findAll()).thenReturn(List.of(sampleBrand));

        List<Brand> result = brandService.getAllBrands();

        assertEquals(1, result.size());
        verify(brandRepository).findAll();
    }

    @Test
    @DisplayName("Add brand successfully")
    void addBrand_Success() {
        UpsertBrandRequest request = new UpsertBrandRequest();
        request.setName(brandName);
        when(brandRepository.existsByName(brandName)).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenReturn(sampleBrand);

        Brand result = brandService.addBrand(request);

        assertNotNull(result);
        assertEquals(brandName, result.getName());
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    @DisplayName("Throw when adding duplicate brand")
    void addBrand_AlreadyExists() {
        UpsertBrandRequest request = new UpsertBrandRequest();
        request.setName(brandName);
        when(brandRepository.existsByName(brandName)).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class,
                () -> brandService.addBrand(request));

        assertEquals(brandName + " already exists", exception.getMessage());
        verify(brandRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update brand successfully with a new name")
    void updateBrand_Success() {
        UpsertBrandRequest request = new UpsertBrandRequest();
        request.setName("New Apple");
        when(brandRepository.existsByName("New Apple")).thenReturn(false);
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(brandRepository.save(sampleBrand)).thenReturn(sampleBrand);

        Brand result = brandService.updateBrand(request, brandId);

        assertNotNull(result);
        assertEquals("New Apple", result.getName());
        verify(brandRepository).save(sampleBrand);
    }

    @Test
    @DisplayName("Allow updating brand when name remains unchanged")
    void updateBrand_SameName_Success() {
        UpsertBrandRequest request = new UpsertBrandRequest();
        request.setName(brandName);
        when(brandRepository.existsByName(brandName)).thenReturn(true);
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(brandRepository.save(sampleBrand)).thenReturn(sampleBrand);

        Brand result = brandService.updateBrand(request, brandId);

        assertEquals(brandName, result.getName());
        verify(brandRepository).save(sampleBrand);
    }

    @Test
    @DisplayName("Throw when updating to a duplicate name owned by another brand")
    void updateBrand_AlreadyExists() {
        UpsertBrandRequest request = new UpsertBrandRequest();
        request.setName("Samsung");
        when(brandRepository.existsByName("Samsung")).thenReturn(true);
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class,
                () -> brandService.updateBrand(request, brandId));

        assertEquals("Samsung already exists", exception.getMessage());
        verify(brandRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete brand successfully")
    void deleteBrandById_Success() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        doNothing().when(brandRepository).delete(sampleBrand);

        assertDoesNotThrow(() -> brandService.deleteBrandById(brandId));

        verify(brandRepository).delete(sampleBrand);
    }

    @Test
    @DisplayName("Throw when deleting missing brand")
    void deleteBrandById_NotFound() {
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        BrandNotFoundException exception = assertThrows(BrandNotFoundException.class,
                () -> brandService.deleteBrandById(brandId));

        assertEquals("Brand not found!!", exception.getMessage());
        verify(brandRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Get all brands with paging successfully")
    void getAllBrandsWithPaging_Success() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Brand> page = new PageImpl<>(List.of(sampleBrand), pageable, 1);
        when(brandRepository.findAll(pageable)).thenReturn(page);

        Page<Brand> result = brandService.getAllBrandsWithPaging(pageable);

        assertEquals(1, result.getContent().size());
        verify(brandRepository, times(1)).findAll(pageable);
    }
}
