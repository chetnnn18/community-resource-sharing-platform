package com.sharenest.platform.controller;

import com.sharenest.platform.service.CategoryService;
import com.sharenest.platform.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ItemService itemService;
    private final CategoryService categoryService;

    public HomeController(ItemService itemService, CategoryService categoryService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latestItems", itemService.latestAvailable());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("totalItems", itemService.countAll());
        model.addAttribute("availableItems", itemService.countAvailable());
        return "home";
    }
}
