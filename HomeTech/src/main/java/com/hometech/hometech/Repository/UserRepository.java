package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Account;
import com.hometech.hometech.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByGoogleId(String googleId);
    User findByAccount(Account account);
    User findByEmail(String email);
    long countByActive(boolean active);
    List<User> findByFullNameContainingIgnoreCaseOrAccount_EmailContainingIgnoreCase(String fullName, String email);
    List<User> findByAccount_EmailIsNotNull();
    Page<User> findByAccount_EmailIsNotNull(Pageable pageable);

}
