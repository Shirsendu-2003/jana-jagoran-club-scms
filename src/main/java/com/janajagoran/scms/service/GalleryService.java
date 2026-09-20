package com.janajagoran.scms.service;

import com.janajagoran.scms.entity.*;
import com.janajagoran.scms.enums.ApprovalStatus;
import com.janajagoran.scms.exception.BadRequestException;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.EventRepository;
import com.janajagoran.scms.repository.GalleryImageRepository;
import com.janajagoran.scms.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Gallery createAlbum(String title, Long eventId, User createdBy) {
        Event event = eventId != null ? eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found")) : null;
        Gallery gallery = Gallery.builder()
                .title(title)
                .event(event)
                .createdBy(createdBy)
                .build();
        return galleryRepository.save(gallery);
    }

    public List<Gallery> getAllAlbums() {
        return galleryRepository.findAll();
    }

    @Transactional
    public GalleryImage uploadImage(Long galleryId, String imageUrl, User uploadedBy) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found"));
        GalleryImage image = GalleryImage.builder()
                .gallery(gallery)
                .imageUrl(imageUrl)
                .uploadedBy(uploadedBy)
                .status(ApprovalStatus.PENDING)
                .build();
        return galleryImageRepository.save(image);
    }

    public List<GalleryImage> getImagesForAlbum(Long galleryId) {
        return galleryImageRepository.findByGalleryId(galleryId);
    }

    public List<GalleryImage> getPendingImages() {
        return galleryImageRepository.findByStatus(ApprovalStatus.PENDING);
    }

    public List<GalleryImage> getApprovedImages() {
        return galleryImageRepository.findByStatus(ApprovalStatus.APPROVED);
    }

    public List<GalleryImage> getFeaturedImages() {
        return galleryImageRepository.findByIsFeaturedTrue();
    }

    /**
     * Public-safe subset for the landing page: only images that are BOTH approved
     * and marked featured. Being featured alone isn't enough -- an Admin must have
     * also approved it, so nothing pending/rejected can leak onto the public site.
     */
    public List<GalleryImage> getPublicShowcase() {
        return galleryImageRepository.findByIsFeaturedTrue().stream()
                .filter(img -> img.getStatus() == ApprovalStatus.APPROVED)
                .toList();
    }

    public long countApprovedImages() {
        return galleryImageRepository.findByStatus(ApprovalStatus.APPROVED).size();
    }

    @Transactional
    public GalleryImage approveImage(Long imageId, boolean approve) {
        GalleryImage image = galleryImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        image.setStatus(approve ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        return galleryImageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Long imageId, Long requestingUserId, boolean isAdmin) {
        GalleryImage image = galleryImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        boolean isOwner = image.getUploadedBy().getId().equals(requestingUserId);
        if (!isAdmin && !isOwner) {
            throw new BadRequestException("You can only delete your own uploads");
        }
        if (!isAdmin && image.getStatus() != ApprovalStatus.PENDING) {
            throw new BadRequestException("Approved photos can no longer be deleted by the uploader");
        }
        galleryImageRepository.delete(image);
    }

    @Transactional
    public GalleryImage setFeatured(Long imageId, boolean featured) {
        GalleryImage image = galleryImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        image.setIsFeatured(featured);
        return galleryImageRepository.save(image);
    }
}
