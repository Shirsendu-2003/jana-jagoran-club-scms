package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.HomepageContentRequest;
import com.janajagoran.scms.entity.HomepageContent;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.repository.HomepageContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomepageService {

    private final HomepageContentRepository repository;

    public List<HomepageContent> getAllSections() {
        return repository.findAll();
    }

    @Transactional
    public HomepageContent upsertSection(HomepageContentRequest request, User updatedBy) {
        HomepageContent section = repository.findBySectionKey(request.getSectionKey())
                .orElse(HomepageContent.builder().sectionKey(request.getSectionKey()).build());

        section.setTitle(request.getTitle());
        section.setBody(request.getBody());
        section.setImageUrl(request.getImageUrl());
        section.setUpdatedBy(updatedBy);

        return repository.save(section);
    }
}
