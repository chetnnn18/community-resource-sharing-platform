package com.sharenest.platform.controller;

import com.sharenest.platform.service.BorrowRequestService;
import com.sharenest.platform.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;
    private final CurrentUserService currentUserService;

    public BorrowRequestController(BorrowRequestService borrowRequestService, CurrentUserService currentUserService) {
        this.borrowRequestService = borrowRequestService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/requests/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        borrowRequestService.approve(id, currentUserService.getCurrentUser());
        redirectAttributes.addFlashAttribute("success", "Borrow request approved.");
        return "redirect:/dashboard";
    }

    @PostMapping("/requests/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        borrowRequestService.reject(id, currentUserService.getCurrentUser());
        redirectAttributes.addFlashAttribute("success", "Borrow request rejected.");
        return "redirect:/dashboard";
    }

    @PostMapping("/requests/{id}/return")
    public String returned(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        borrowRequestService.markReturned(id, currentUserService.getCurrentUser());
        redirectAttributes.addFlashAttribute("success", "Item marked as returned.");
        return "redirect:/dashboard";
    }

    @PostMapping("/requests/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        borrowRequestService.cancel(id, currentUserService.getCurrentUser());
        redirectAttributes.addFlashAttribute("success", "Borrow request cancelled.");
        return "redirect:/dashboard";
    }
}
