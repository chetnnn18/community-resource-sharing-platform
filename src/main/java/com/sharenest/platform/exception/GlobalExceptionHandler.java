package com.sharenest.platform.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String notFound(ResourceNotFoundException exception, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(AccessDeniedForResourceException.class)
    public String forbidden(AccessDeniedForResourceException exception, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("message", exception.getMessage());
        return "error";
    }
}
