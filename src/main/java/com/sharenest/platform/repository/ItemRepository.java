package com.sharenest.platform.repository;

import com.sharenest.platform.entity.Category;
import com.sharenest.platform.entity.Item;
import com.sharenest.platform.entity.ItemStatus;
import com.sharenest.platform.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Override
    @EntityGraph(attributePaths = {"category", "owner"})
    Optional<Item> findById(Long id);

    @EntityGraph(attributePaths = {"category", "owner"})
    List<Item> findByOwnerOrderByCreatedAtDesc(User owner);

    @EntityGraph(attributePaths = {"category", "owner"})
    List<Item> findTop6ByStatusOrderByCreatedAtDesc(ItemStatus status);

    long countByOwner(User owner);

    long countByStatus(ItemStatus status);

    long countByCategory(Category category);

    @Query("""
            select i from Item i
            join fetch i.category
            join fetch i.owner
            where (:keyword is null or lower(i.title) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or i.category.id = :categoryId)
            order by i.createdAt desc
            """)
    List<Item> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
