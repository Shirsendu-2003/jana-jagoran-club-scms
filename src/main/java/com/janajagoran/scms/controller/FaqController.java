package com.janajagoran.scms.controller;

import com.janajagoran.scms.dto.ApiResponse;
import com.janajagoran.scms.dto.FaqRequest;
import com.janajagoran.scms.entity.Faq;
import com.janajagoran.scms.entity.User;
import com.janajagoran.scms.security.UserPrincipal;
import com.janajagoran.scms.service.FaqService;
import com.janajagoran.scms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Faq>>> allFaqs() {
        return ResponseEntity.ok(ApiResponse.ok("FAQs fetched", faqService.getAllFaqs()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Faq>> createFaq(
            @Valid @RequestBody FaqRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getReference(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("FAQ created", faqService.createFaq(request, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Faq>> updateFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("FAQ updated", faqService.updateFaq(id, request)));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<Faq>> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok(active ? "FAQ activated" : "FAQ deactivated", faqService.toggleActive(id, active)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.ok(ApiResponse.ok("FAQ deleted"));
    }
}
