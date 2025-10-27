package com.hometech.hometech.service;

import com.hometech.hometech.Repository.AccountReposirory;
import com.hometech.hometech.enums.RoleType;
import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.User;
import com.hometech.hometech.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountReposirory accountReposirory;

    public UserService(UserRepository userRepository, AccountReposirory accountReposirory) {
        this.userRepository = userRepository;
        this.accountReposirory = accountReposirory;
    }

    // Lấy danh sách tất cả người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Cập nhật trạng thái hoạt động
    public void updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        user.setActive(active);
        userRepository.save(user);
    }
    public void updateUserRole(Long id, RoleType roleName) {
        Account account = accountReposirory.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        account.setRole(roleName);
        accountReposirory.save(account);
    }
    public long countAll() { return userRepository.count(); }
    public long countByStatus(boolean active) { return userRepository.countByActive(active); }
    // 🟢 Tìm kiếm người dùng
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(); // nếu ô tìm kiếm trống -> trả tất cả
        }
        return userRepository.findByFullNameContainingIgnoreCaseOrAccount_EmailContainingIgnoreCase(keyword, keyword);
    }
}
