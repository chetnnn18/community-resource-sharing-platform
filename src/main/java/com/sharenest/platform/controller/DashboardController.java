package com.sharenest.platform.controller;

import com.sharenest.platform.entity.User;
import com.sharenest.platform.service.BorrowRequestService;
import com.sharenest.platform.service.CurrentUserService;
import com.sharenest.platform.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final CurrentUserService currentUserService;
    private final ItemService itemService;
    private final BorrowRequestService borrowRequestService;

    public DashboardController(CurrentUserService currentUserService,
                               ItemService itemService,
                               BorrowRequestService borrowRequestService) {
        this.currentUserService = currentUserService;
        this.itemService = itemService;
        this.borrowRequestService = borrowRequestService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = currentUserService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("myItems", itemService.findByOwner(user));
        model.addAttribute("incomingRequests", borrowRequestService.forOwner(user));
        model.addAttribute("myRequests", borrowRequestService.forBorrower(user));
        model.addAttribute("itemCount", itemService.countByOwner(user));
        model.addAttribute("incomingCount", borrowRequestService.countRequestsForOwner(user));
        model.addAttribute("borrowCount", borrowRequestService.countBorrowedBy(user));
        return "dashboard/index";
    }
}
