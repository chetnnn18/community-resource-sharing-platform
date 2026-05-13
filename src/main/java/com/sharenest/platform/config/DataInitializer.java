package com.sharenest.platform.config;

import com.sharenest.platform.entity.Category;
import com.sharenest.platform.entity.Item;
import com.sharenest.platform.entity.ItemStatus;
import com.sharenest.platform.entity.ResourceApprovalStatus;
import com.sharenest.platform.entity.Role;
import com.sharenest.platform.entity.User;
import com.sharenest.platform.repository.CategoryRepository;
import com.sharenest.platform.repository.ItemRepository;
import com.sharenest.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final boolean seedData;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(@Value("${app.seed-data:true}") boolean seedData,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ItemRepository itemRepository,
                           PasswordEncoder passwordEncoder) {
        this.seedData = seedData;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureDefaultCategories();

        if (!seedData || userRepository.count() > 0) {
            return;
        }

        User admin = user("Admin User", "admin@sharenest.local", "9876543210", "Community Office", Role.ADMIN);
        User priya = user("Priya Sharma", "priya@sharenest.local", "9876500001", "Block A, Flat 102", Role.USER);
        User arjun = user("Arjun Mehta", "arjun@sharenest.local", "9876500002", "Block B, Flat 305", Role.USER);
        userRepository.saveAll(List.of(admin, priya, arjun));

        Category tools = categoryRepository.save(new Category("Tools", "Drills, ladders, repair kits and home tools"));
        Category kitchen = categoryRepository.save(new Category("Kitchen", "Appliances and cookware for occasional use"));
        Category books = categoryRepository.save(new Category("Books", "Books, guides and learning material"));
        Category outdoor = categoryRepository.save(new Category("Outdoor", "Sports, garden and travel equipment"));

        itemRepository.save(item("Cordless Drill Kit", "18V drill with bits, charger and safety case.", "Block A Lobby", tools, priya,
                "https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=900&q=80"));
        itemRepository.save(item("Large Pressure Cooker", "Eight litre pressure cooker for gatherings and festivals.", "Block B Security Desk", kitchen, arjun,
                "https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=900&q=80"));
        itemRepository.save(item("Weekend Camping Tent", "Four-person waterproof tent with pegs and carry bag.", "Clubhouse Store", outdoor, priya,
                "https://images.unsplash.com/photo-1504851149312-7a075b496cc7?auto=format&fit=crop&w=900&q=80"));
        itemRepository.save(item("Java Programming Book", "Beginner friendly Java guide in good condition.", "Block B, Flat 305", books, arjun,
                "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=900&q=80"));
    }

    private User user(String name, String email, String phone, String address, Role role) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole(role);
        return user;
    }

    private Item item(String title, String description, String location, Category category, User owner, String imageUrl) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setLocation(location);
        item.setCategory(category);
        item.setOwner(owner);
        item.setImageUrl(imageUrl);
        item.setPrice(BigDecimal.ZERO);
        item.setStatus(ItemStatus.AVAILABLE);
        item.setApprovalStatus(ResourceApprovalStatus.APPROVED);
        return item;
    }

    private void ensureDefaultCategories() {
        List<Category> categories = List.of(
                new Category("Daily Essentials", "Everyday household basics and regularly used items"),
                new Category("Personal Care", "Grooming, wellness and personal care items"),
                new Category("Kitchen Items", "Cookware, utensils and kitchen accessories"),
                new Category("Electronics", "Devices, chargers, accessories and small electronics"),
                new Category("Books & Study Material", "Books, notes, guides and learning resources"),
                new Category("Sports Equipment", "Fitness, games and outdoor sports gear"),
                new Category("Travel Accessories", "Bags, organizers and travel-use items"),
                new Category("Baby Care", "Baby essentials, toys and child care items"),
                new Category("Medical Supplies", "Basic medical, mobility and care supplies"),
                new Category("Tools & Hardware", "Repair tools, hardware and maintenance equipment"),
                new Category("Fashion & Clothing", "Clothes, accessories and occasion wear"),
                new Category("Home Appliances", "Small and large appliances for home use")
        );

        categories.forEach(category -> categoryRepository.findByNameIgnoreCase(category.getName())
                .orElseGet(() -> categoryRepository.save(category)));
    }
}
