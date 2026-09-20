package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.GalleryImage;
import com.janajagoran.scms.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {
    List<GalleryImage> findByGalleryId(Long galleryId);
    List<GalleryImage> findByStatus(ApprovalStatus status);
    List<GalleryImage> findByUploadedById(Long userId);
    List<GalleryImage> findByIsFeaturedTrue();
}
