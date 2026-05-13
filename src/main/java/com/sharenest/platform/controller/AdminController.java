package com.sharenest.platform.controller;

import com.sharenest.platform.entity.Category;
import com.sharenest.platform.service.BorrowRequestService;
import com.sharenest.platform.service.CategoryService;
import com.sharenest.platform.service.ItemService;
import com.sharenest.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final UserService userService;
    private final ItemService itemService;
    private final CategoryService categoryService;
    private final BorrowRequestService borrowRequestService;

    public AdminController(UserService userService,
                           ItemService itemService,
                           CategoryService categoryService,
                           BorrowRequestService borrowRequestService) {
        this.userService = userService;
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.borrowRequestService = borrowRequestService;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("regularUserCount", userService.countRegularUsers());
        model.addAttribute("itemCount", itemService.countAll());
        model.addAttribute("availableItemCount", itemService.countAvailable());
        model.addAttribute("pendingResourceCount", itemService.countPendingApproval());
        model.addAttribute("requestCount", borrowRequestService.countAll());
        model.addAttribute("pendingRequestCount", borrowRequestService.countPending());
        model.addAttribute("recentUsers", userService.recentUsers());
        return "admin/index";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/items")
    public String items(Model model) {
        model.addAttribute("items", itemService.search(null, null));
        return "admin/items";
    }

    @GetMapping("/admin/resources/pending")
    public String pendingResources(Model model) {
        model.addAttribute("items", itemService.findPendingApproval());
        return "admin/pending-resources";
    }

    @PutMapping("/admin/resources/{id}/approve")
    public ResponseEntity<Void> approveResource(@PathVariable Long id) {
        itemService.approveResource(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/resources/{id}/reject")
    public ResponseEntity<Void> rejectResource(@PathVariable Long id) {
        itemService.rejectResource(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/resources/{id}/approve")
    public String approveResourceFallback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        itemService.approveResource(id);
        redirectAttributes.addFlashAttribute("success", "Resource approved.");
        return "redirect:/admin/resources/pending";
    }

    @PostMapping("/admin/resources/{id}/reject")
    public String rejectResourceFallback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        itemService.rejectResource(id);
        redirectAttributes.addFlashAttribute("success", "Resource rejected.");
        return "redirect:/admin/resources/pending";
    }

    @GetMapping("/admin/requests")
    public String requests(Model model) {
        model.addAttribute("requests", borrowRequestService.allRequests());
        return "admin/requests";
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @PostMapping("/admin/categories")
    public String saveCategory(@Valid @ModelAttribute Category category,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/categories";
        }

        try {
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Category saved.");
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("name", "duplicate", exception.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/categories";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
