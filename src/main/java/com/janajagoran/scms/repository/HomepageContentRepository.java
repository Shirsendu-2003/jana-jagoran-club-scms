package com.janajagoran.scms.repository;

import com.janajagoran.scms.entity.HomepageContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomepageContentRepository extends JpaRepository<HomepageContent, Long> {
    Optional<HomepageContent> findBySectionKey(String sectionKey);
}
