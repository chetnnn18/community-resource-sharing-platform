package com.sharenest.platform.service;

import com.sharenest.platform.dto.RegistrationForm;
import com.sharenest.platform.entity.Role;
import com.sharenest.platform.entity.User;
import com.sharenest.platform.exception.ResourceNotFoundException;
import com.sharenest.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegistrationForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> recentUsers() {
        return userRepository.findTop6ByOrderByCreatedAtDesc();
    }

    public void toggleEnabled(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long countRegularUsers() {
        return userRepository.countByRole(Role.USER);
    }
}
