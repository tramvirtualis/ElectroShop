package com.hometech.hometech.service;

import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.model.Address;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.User;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.Repository.UserRepository;
import com.hometech.hometech.Repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ProfileService {

    public ProfileService(CustomerRepository customerRepository, UserRepository userRepository, AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public User updateOrCreateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 🔹 Cập nhật thông tin user từ DTO
        user.setName(dto.getName());
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        if (dto.getPictureUrl() != null && !dto.getPictureUrl().isEmpty()) {
            user.setPictureUrl(dto.getPictureUrl());
        }
        
        // 🔹 Lưu user (keep effectively final for lambda usage below)
        final User savedUser = userRepository.save(user);

        // 🔹 Cập nhật ngày sinh & địa chỉ vào Customer/Address
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setUser(savedUser);
                    return customerRepository.save(c);
                });

        // Date of birth parsing yyyy-MM-dd
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            try {
                java.util.Date dob = java.sql.Date.valueOf(dto.getDateOfBirth());
                customer.setDateOfBirth(dob);
            } catch (IllegalArgumentException ignored) {}
        }

        // Find existing address using both sides to avoid duplicates
        Address address = null;
        if (customer.getAddress() != null) {
            address = customer.getAddress();
        }
        if (address == null) {
            address = addressRepository.findByCustomer_Id(customer.getId()).orElse(null);
        }
        if (address == null) {
            address = new Address();
        }
        if (dto.getAddressLine() != null) address.setAddressLine(dto.getAddressLine());
        if (dto.getCommune() != null) address.setCommune(dto.getCommune());
        if (dto.getCity() != null) address.setCity(dto.getCity());

        // Persist address first to obtain address_id, then link to customer and persist customer
        address.setCustomer(customer);
        address = addressRepository.save(address);
        customer.setAddress(address);
        customerRepository.save(customer);

        return savedUser;
    }
    public UpdateProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName(user.getName());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setPictureUrl(user.getPictureUrl());

        // Address & DOB
        Customer customer = customerRepository.findByUser_Id(userId).orElse(null);
        if (customer != null) {
            if (customer.getDateOfBirth() != null) {
                dto.setDateOfBirth(new java.text.SimpleDateFormat("yyyy-MM-dd").format(customer.getDateOfBirth()));
            }
            // Prefer attached address; fallback to repository using customer.id
            Address a = customer.getAddress();
            if (a == null) {
                a = addressRepository.findByCustomer_Id(customer.getId()).orElse(null);
            }
            if (a != null) {
                dto.setAddressLine(a.getAddressLine());
                dto.setCommune(a.getCommune());
                dto.setCity(a.getCity());
            }
        }

        return dto;
    }
    
    public Long getUserIdByUsername(String username) {
        User user = null;
        
        // First try to find by Google ID (for OAuth users) - the auth name is often the Google ID
        user = userRepository.findByGoogleId(username);
        
        // If not found, try to find by username in account
        if (user == null) {
            user = userRepository.findAll().stream()
                    .filter(u -> u.getAccount() != null && username.equals(u.getAccount().getUsername()))
                    .findFirst()
                    .orElse(null);
        }
        
        // If not found, try to find by email (for OAuth users)
        if (user == null) {
            user = userRepository.findAll().stream()
                    .filter(u -> u.getAccount() != null && username.equals(u.getAccount().getEmail()))
                    .findFirst()
                    .orElse(null);
        }
        
        // If still not found, try to find directly by email in users table
        if (user == null) {
            user = userRepository.findByEmail(username);
        }
        
        return user != null ? user.getId() : null;
    }
    
    public Long getUserIdByEmail(String email) {
        // First try to find in users table by email
        User user = userRepository.findByEmail(email);
        
        // If not found, try to find by account email
        if (user == null) {
            user = userRepository.findAll().stream()
                    .filter(u -> u.getAccount() != null && email.equals(u.getAccount().getEmail()))
                    .findFirst()
                    .orElse(null);
        }
        
        return user != null ? user.getId() : null;
    }
    
    public String getEmailByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Try user.email first, then account.email
            if (user.getEmail() != null) {
                return user.getEmail();
            }
            if (user.getAccount() != null) {
                return user.getAccount().getEmail();
            }
        }
        return null;
    }
    
    public String getUsernameByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getAccount() != null) {
            return user.getAccount().getUsername();
        }
        return null;
    }

    @Transactional
    public void linkGoogleIdIfMissing(Long userId, String googleSub) {
        if (googleSub == null || googleSub.isEmpty()) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        if (user.getGoogleId() == null || user.getGoogleId().isEmpty()) {
            user.setGoogleId(googleSub);
            userRepository.save(user);
        }
    }

    @Transactional
    public String storeProfileImage(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        // Store under /uploads/avatars/{userId}/avatar.ext
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
        Path uploadRoot = Paths.get("src/main/resources/static/uploads/avatars/" + userId);
        Files.createDirectories(uploadRoot);
        Path target = uploadRoot.resolve("avatar" + ext);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Public URL mapping via Spring static resources
        String publicUrl = "/uploads/avatars/" + userId + "/avatar" + ext;

        // Persist URL to user
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setPictureUrl(publicUrl);
            userRepository.save(user);
        }
        return publicUrl;
    }
}
