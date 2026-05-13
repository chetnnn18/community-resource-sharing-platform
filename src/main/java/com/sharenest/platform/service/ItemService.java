package com.sharenest.platform.service;

import com.sharenest.platform.dto.ItemForm;
import com.sharenest.platform.entity.Category;
import com.sharenest.platform.entity.Item;
import com.sharenest.platform.entity.ItemStatus;
import com.sharenest.platform.entity.ResourceApprovalStatus;
import com.sharenest.platform.entity.Role;
import com.sharenest.platform.entity.User;
import com.sharenest.platform.exception.AccessDeniedForResourceException;
import com.sharenest.platform.exception.ResourceNotFoundException;
import com.sharenest.platform.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public ItemService(ItemRepository itemRepository,
                       CategoryService categoryService,
                       FileStorageService fileStorageService) {
        this.itemRepository = itemRepository;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<Item> search(String keyword, Long categoryId) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return itemRepository.search(normalizedKeyword, categoryId);
    }

    @Transactional(readOnly = true)
    public List<Item> searchApproved(String keyword, Long categoryId) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return itemRepository.searchByApprovalStatus(normalizedKeyword, categoryId, ResourceApprovalStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Item> latestAvailable() {
        return itemRepository.findTop6ByStatusAndApprovalStatusOrderByCreatedAtDesc(
                ItemStatus.AVAILABLE,
                ResourceApprovalStatus.APPROVED
        );
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

    @Transactional(readOnly = true)
    public Item findPublicById(Long id) {
        Item item = findById(id);
        if (item.getApprovalStatus() != ResourceApprovalStatus.APPROVED) {
            throw new ResourceNotFoundException("Item not found");
        }
        return item;
    }

    @Transactional(readOnly = true)
    public Item findVisibleToUser(Long id, User viewer) {
        Item item = findById(id);
        if (item.getApprovalStatus() == ResourceApprovalStatus.APPROVED) {
            return item;
        }
        boolean owner = viewer != null && item.getOwner().getId().equals(viewer.getId());
        boolean admin = viewer != null && viewer.getRole() == Role.ADMIN;
        if (!owner && !admin) {
            throw new ResourceNotFoundException("Item not found");
        }
        return item;
    }

    @Transactional(readOnly = true)
    public Item findManageableById(Long id, User actor) {
        Item item = findById(id);
        ensureOwnerOrAdmin(item, actor);
        return item;
    }

    @Transactional(readOnly = true)
    public List<Item> findByOwner(User owner) {
        return itemRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public List<Item> findPendingApproval() {
        return itemRepository.findByApprovalStatusOrderByCreatedAtDesc(ResourceApprovalStatus.PENDING);
    }

    public Item create(ItemForm form, User owner) {
        Item item = new Item();
        copyFormToItem(form, item);
        item.setOwner(owner);
        item.setStatus(ItemStatus.AVAILABLE);
        // New submissions are intentionally hidden until an admin reviews them.
        item.setApprovalStatus(ResourceApprovalStatus.PENDING);
        return itemRepository.save(item);
    }

    public Item update(Long id, ItemForm form, User actor) {
        Item item = findById(id);
        ensureOwnerOrAdmin(item, actor);
        copyFormToItem(form, item);
        if (actor.getRole() != Role.ADMIN) {
            item.setApprovalStatus(ResourceApprovalStatus.PENDING);
        }
        return itemRepository.save(item);
    }

    public void delete(Long id, User actor) {
        Item item = findById(id);
        ensureOwnerOrAdmin(item, actor);
        itemRepository.delete(item);
    }

    public ItemForm toForm(Item item) {
        ItemForm form = new ItemForm();
        form.setTitle(item.getTitle());
        form.setDescription(item.getDescription());
        form.setLocation(item.getLocation());
        form.setImageUrl(item.getImageUrl());
        form.setPrice(item.getPrice());
        form.setStatus(item.getStatus());
        form.setCategoryId(item.getCategory().getId());
        return form;
    }

    public void markBorrowed(Item item) {
        item.setStatus(ItemStatus.BORROWED);
    }

    public void markAvailable(Item item) {
        item.setStatus(ItemStatus.AVAILABLE);
    }

    public void markReturned(Item item) {
        item.setStatus(ItemStatus.RETURNED);
    }

    public void approveResource(Long id) {
        Item item = findById(id);
        item.setApprovalStatus(ResourceApprovalStatus.APPROVED);
    }

    public void rejectResource(Long id) {
        Item item = findById(id);
        item.setApprovalStatus(ResourceApprovalStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return itemRepository.count();
    }

    @Transactional(readOnly = true)
    public long countApproved() {
        return itemRepository.countByApprovalStatus(ResourceApprovalStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return itemRepository.countByStatusAndApprovalStatus(ItemStatus.AVAILABLE, ResourceApprovalStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public long countPendingApproval() {
        return itemRepository.countByApprovalStatus(ResourceApprovalStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long countByOwner(User owner) {
        return itemRepository.countByOwner(owner);
    }

    private void copyFormToItem(ItemForm form, Item item) {
        Category category = categoryService.findById(form.getCategoryId());
        item.setTitle(form.getTitle());
        item.setDescription(form.getDescription());
        item.setLocation(form.getLocation());
        item.setPrice(form.getPrice());
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            item.setImageUrl(fileStorageService.storeImage(form.getImageFile()));
        } else if (StringUtils.hasText(form.getImageUrl())) {
            item.setImageUrl(form.getImageUrl());
        } else {
            item.setImageUrl(null);
        }
        item.setStatus(form.getStatus() == null ? ItemStatus.AVAILABLE : form.getStatus());
        item.setCategory(category);
    }

    private void ensureOwnerOrAdmin(Item item, User actor) {
        boolean owner = item.getOwner().getId().equals(actor.getId());
        boolean admin = actor.getRole() == Role.ADMIN;
        if (!owner && !admin) {
            throw new AccessDeniedForResourceException("You do not have permission to manage this item.");
        }
    }
}
