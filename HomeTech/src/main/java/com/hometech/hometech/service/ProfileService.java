package com.hometech.hometech.service;

import com.hometech.hometech.dto.UpdateProfileDTO;
import com.hometech.hometech.model.Address;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.User;
import com.hometech.hometech.Repository.CustomerRepository;
import com.hometech.hometech.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    public ProfileService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional
    public Customer updateOrCreateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 🔹 Nếu Customer chưa tồn tại thì tạo mới
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setUser(user);
                    return newCustomer;
                });

        // 🔹 Cập nhật thông tin user cơ bản
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());

        // 🔹 Nếu chưa có Address thì tạo mới
        Address address = customer.getAddress();
        if (address == null) {
            address = new Address();
            address.setCustomer(customer);
        }

        address.setAddressLine(dto.getAddressLine());
        address.setCommune(dto.getCommune());
        address.setCity(dto.getCity());
        customer.setAddress(address);

        // 🔹 Lưu cả user & customer
        userRepository.save(user);
        return customerRepository.save(customer);
    }
    public UpdateProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Customer customer = customerRepository.findByUser_Id(userId).orElse(null);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());

        if (customer != null && customer.getAddress() != null) {
            Address address = customer.getAddress();
            dto.setAddressLine(address.getAddressLine());
            dto.setCommune(address.getCommune());
            dto.setCity(address.getCity());
        }

        return dto;
    }
}
