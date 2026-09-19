package com.certificate.system.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds common model attributes available to ALL Thymeleaf templates.
 *
 * In Thymeleaf 3.1+, the #httpServletRequest and #request utility objects
 * are no longer available due to security hardening. This advice exposes
 * the current request URI as a model attribute so templates can use it
 * for active navigation highlighting.
 */
@ControllerAdvice
public class GlobalModelAttributeAdvice {

    /**
     * Exposes the current HTTP request URI so Thymeleaf templates can
     * determine the active navigation link without accessing servlet API objects directly.
     *
     * Usage in templates:
     *   th:classappend="${requestUri.contains('/dashboard') ? 'active' : ''}"
     */
    @ModelAttribute("requestUri")
    public String requestUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
