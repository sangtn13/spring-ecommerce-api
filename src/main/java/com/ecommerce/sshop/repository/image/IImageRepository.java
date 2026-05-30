package com.ecommerce.sshop.repository.image;

import java.util.List;

import com.ecommerce.sshop.model.image.Image;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IImageRepository extends JpaRepository<Image, String> {
    List<Image> findByProductId(String id);
}
