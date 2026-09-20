package com.janajagoran.scms.service;

import com.janajagoran.scms.dto.FaqRequest;
import com.janajagoran.scms.entity.Faq;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.exception.ResourceNotFoundException;
import com.janajagoran.scms.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public List<Faq> getPublicFaqs() {
        return faqRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    public List<Faq> getAllFaqs() {
        return faqRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    public Faq createFaq(FaqRequest request, User createdBy) {
        Faq faq = Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .category(request.getCategory())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .createdBy(createdBy)
                .build();
        return faqRepository.save(faq);
    }

    @Transactional
    public Faq updateFaq(Long id, FaqRequest request) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FAQ not found"));
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setCategory(request.getCategory());
        if (request.getDisplayOrder() != null) faq.setDisplayOrder(request.getDisplayOrder());
        return faqRepository.save(faq);
    }

    @Transactional
    public Faq toggleActive(Long id, boolean active) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FAQ not found"));
        faq.setIsActive(active);
        return faqRepository.save(faq);
    }

    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) throw new ResourceNotFoundException("FAQ not found");
        faqRepository.deleteById(id);
    }
}
