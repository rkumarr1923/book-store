package com.bookstore.controller;

import com.bookstore.common.response.ApiResponse;
import com.bookstore.dto.request.ContactRequest;
import com.bookstore.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoint for the Contact Us form.
 * No authentication required — permitted in {@link com.bookstore.config.SecurityConfig}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    /**
     * Receives a contact form submission and forwards it via email.
     *
     * @param request validated contact form payload
     * @return 200 OK with a success message
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendContactMessage(
            @Valid @RequestBody ContactRequest request) {

        log.info("Contact form submission from '{}' <{}>", request.getName(), request.getEmail());
        emailService.sendContactEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("Message sent successfully! We'll get back to you soon."));
    }
}
