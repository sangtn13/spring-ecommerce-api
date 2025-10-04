package com.ecommerce.sshop.repository;

import com.ecommerce.sshop.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IImageRepository extends JpaRepository<Image, Long>  {
    List<Image> findByProductId(Long id);
}
