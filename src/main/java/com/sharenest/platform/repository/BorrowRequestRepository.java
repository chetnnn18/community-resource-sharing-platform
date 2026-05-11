package com.sharenest.platform.repository;

import com.sharenest.platform.entity.BorrowRequest;
import com.sharenest.platform.entity.BorrowStatus;
import com.sharenest.platform.entity.Item;
import com.sharenest.platform.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    @Override
    @EntityGraph(attributePaths = {"item", "item.owner", "item.category", "borrower"})
    List<BorrowRequest> findAll();

    @Override
    @EntityGraph(attributePaths = {"item", "item.owner", "item.category", "borrower"})
    Optional<BorrowRequest> findById(Long id);

    @EntityGraph(attributePaths = {"item", "item.owner", "item.category", "borrower"})
    List<BorrowRequest> findByBorrowerOrderByCreatedAtDesc(User borrower);

    @EntityGraph(attributePaths = {"item", "item.owner", "item.category", "borrower"})
    List<BorrowRequest> findByItemOwnerOrderByCreatedAtDesc(User owner);

    @EntityGraph(attributePaths = {"item", "item.owner", "item.category", "borrower"})
    List<BorrowRequest> findByItemOrderByCreatedAtDesc(Item item);

    long countByBorrower(User borrower);

    long countByItemOwner(User owner);

    long countByStatus(BorrowStatus status);

    boolean existsByItemAndBorrowerAndStatus(Item item, User borrower, BorrowStatus status);
}
