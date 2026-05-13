package com.sharenest.platform.controller;

import com.sharenest.platform.dto.ItemForm;
import com.sharenest.platform.entity.BorrowRequest;
import com.sharenest.platform.entity.Item;
import com.sharenest.platform.entity.ItemStatus;
import com.sharenest.platform.entity.User;
import com.sharenest.platform.service.BorrowRequestService;
import com.sharenest.platform.service.CategoryService;
import com.sharenest.platform.service.CurrentUserService;
import com.sharenest.platform.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;
    private final BorrowRequestService borrowRequestService;

    public ItemController(ItemService itemService,
                          CategoryService categoryService,
                          CurrentUserService currentUserService,
                          BorrowRequestService borrowRequestService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
        this.borrowRequestService = borrowRequestService;
    }

    @GetMapping("/items")
    public String browse(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long categoryId,
                         Model model) {
        model.addAttribute("items", itemService.searchApproved(keyword, categoryId));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "items/list";
    }

    @GetMapping("/items/view/{id}")
    public String details(@PathVariable Long id, Model model) {
        User currentUser = currentUserService.getCurrentUser();
        Item item = itemService.findVisibleToUser(id, currentUser);
        model.addAttribute("item", item);
        model.addAttribute("borrowRequest", new BorrowRequest());
        model.addAttribute("currentUser", currentUser);
        return "items/details";
    }

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("itemForm", new ItemForm());
        addItemFormOptions(model);
        return "items/form";
    }

    @PostMapping("/items")
    public String create(@Valid @ModelAttribute ItemForm itemForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addItemFormOptions(model);
            return "items/form";
        }

        User currentUser = currentUserService.getCurrentUser();
        try {
            itemService.create(itemForm, currentUser);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("imageFile", "invalid", exception.getMessage());
            addItemFormOptions(model);
            return "items/form";
        }
        redirectAttributes.addFlashAttribute("success", "Item submitted for admin approval.");
        return "redirect:/dashboard";
    }

    @GetMapping("/items/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Item item = itemService.findManageableById(id, currentUserService.getCurrentUser());
        model.addAttribute("itemId", id);
        model.addAttribute("itemForm", itemService.toForm(item));
        addItemFormOptions(model);
        return "items/form";
    }

    @PostMapping("/items/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ItemForm itemForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", id);
            addItemFormOptions(model);
            return "items/form";
        }

        try {
            itemService.update(id, itemForm, currentUserService.getCurrentUser());
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("imageFile", "invalid", exception.getMessage());
            model.addAttribute("itemId", id);
            addItemFormOptions(model);
            return "items/form";
        }
        redirectAttributes.addFlashAttribute("success", "Item updated and sent for admin approval.");
        return "redirect:/items/view/" + id;
    }

    @PostMapping("/items/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        itemService.delete(id, currentUserService.getCurrentUser());
        redirectAttributes.addFlashAttribute("success", "Item deleted successfully.");
        return "redirect:/dashboard";
    }

    @PostMapping("/items/{id}/borrow")
    public String requestBorrow(@PathVariable Long id,
                                @Valid @ModelAttribute BorrowRequest borrowRequest,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            User currentUser = currentUserService.getCurrentUser();
            Item item = itemService.findVisibleToUser(id, currentUser);
            model.addAttribute("item", item);
            model.addAttribute("currentUser", currentUser);
            return "items/details";
        }

        try {
            borrowRequestService.create(id, borrowRequest, currentUserService.getCurrentUser());
            redirectAttributes.addFlashAttribute("success", "Borrow request sent to the item owner.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/items/view/" + id;
    }

    private void addItemFormOptions(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", ItemStatus.values());
    }
}
