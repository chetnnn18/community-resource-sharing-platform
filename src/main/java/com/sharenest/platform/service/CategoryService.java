package com.sharenest.platform.service;

import com.sharenest.platform.entity.Category;
import com.sharenest.platform.exception.ResourceNotFoundException;
import com.sharenest.platform.repository.CategoryRepository;
import com.sharenest.platform.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    public CategoryService(CategoryRepository categoryRepository, ItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public Category save(Category category) {
        categoryRepository.findByNameIgnoreCase(category.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(category.getId())) {
                throw new IllegalArgumentException("Category name already exists.");
            }
        });
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = findById(id);
        if (itemRepository.countByCategory(category) > 0) {
            throw new IllegalArgumentException("This category is used by existing items.");
        }
        categoryRepository.delete(category);
    }
}
