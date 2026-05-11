package com.sharenest.platform.service;

import com.sharenest.platform.entity.BorrowRequest;
import com.sharenest.platform.entity.BorrowStatus;
import com.sharenest.platform.entity.Item;
import com.sharenest.platform.entity.ItemStatus;
import com.sharenest.platform.entity.Role;
import com.sharenest.platform.entity.User;
import com.sharenest.platform.exception.AccessDeniedForResourceException;
import com.sharenest.platform.exception.ResourceNotFoundException;
import com.sharenest.platform.repository.BorrowRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final ItemService itemService;

    public BorrowRequestService(BorrowRequestRepository borrowRequestRepository, ItemService itemService) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.itemService = itemService;
    }

    public BorrowRequest create(Long itemId, BorrowRequest request, User borrower) {
        Item item = itemService.findById(itemId);
        if (item.getOwner().getId().equals(borrower.getId())) {
            throw new IllegalArgumentException("You cannot borrow your own item.");
        }
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new IllegalArgumentException("This item is not available right now.");
        }
        if (request.hasInvalidDateRange()) {
            throw new IllegalArgumentException("End date must be on or after start date.");
        }
        if (borrowRequestRepository.existsByItemAndBorrowerAndStatus(item, borrower, BorrowStatus.PENDING)) {
            throw new IllegalArgumentException("You already have a pending request for this item.");
        }

        request.setItem(item);
        request.setBorrower(borrower);
        request.setStatus(BorrowStatus.PENDING);
        return borrowRequestRepository.save(request);
    }

    public void approve(Long requestId, User actor) {
        BorrowRequest request = findById(requestId);
        ensureOwnerOrAdmin(request, actor);
        request.setStatus(BorrowStatus.APPROVED);
        itemService.markBorrowed(request.getItem());
    }

    public void reject(Long requestId, User actor) {
        BorrowRequest request = findById(requestId);
        ensureOwnerOrAdmin(request, actor);
        request.setStatus(BorrowStatus.REJECTED);
    }

    public void markReturned(Long requestId, User actor) {
        BorrowRequest request = findById(requestId);
        ensureOwnerOrAdmin(request, actor);
        request.setStatus(BorrowStatus.RETURNED);
        itemService.markReturned(request.getItem());
    }

    public void cancel(Long requestId, User actor) {
        BorrowRequest request = findById(requestId);
        if (!request.getBorrower().getId().equals(actor.getId()) && actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedForResourceException("You can only cancel your own request.");
        }
        if (request.getStatus() == BorrowStatus.PENDING) {
            request.setStatus(BorrowStatus.CANCELLED);
        }
    }

    @Transactional(readOnly = true)
    public BorrowRequest findById(Long id) {
        return borrowRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found"));
    }

    @Transactional(readOnly = true)
    public List<BorrowRequest> forBorrower(User borrower) {
        return borrowRequestRepository.findByBorrowerOrderByCreatedAtDesc(borrower);
    }

    @Transactional(readOnly = true)
    public List<BorrowRequest> forOwner(User owner) {
        return borrowRequestRepository.findByItemOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public List<BorrowRequest> forItem(Item item) {
        return borrowRequestRepository.findByItemOrderByCreatedAtDesc(item);
    }

    @Transactional(readOnly = true)
    public List<BorrowRequest> allRequests() {
        return borrowRequestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long countBorrowedBy(User borrower) {
        return borrowRequestRepository.countByBorrower(borrower);
    }

    @Transactional(readOnly = true)
    public long countRequestsForOwner(User owner) {
        return borrowRequestRepository.countByItemOwner(owner);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return borrowRequestRepository.countByStatus(BorrowStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return borrowRequestRepository.count();
    }

    private void ensureOwnerOrAdmin(BorrowRequest request, User actor) {
        boolean owner = request.getItem().getOwner().getId().equals(actor.getId());
        boolean admin = actor.getRole() == Role.ADMIN;
        if (!owner && !admin) {
            throw new AccessDeniedForResourceException("You do not have permission to manage this request.");
        }
    }
}
