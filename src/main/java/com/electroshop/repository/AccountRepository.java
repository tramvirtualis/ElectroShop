package com.electroshop.repository;

import com.electroshop.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT a FROM Account a WHERE a.username = :username AND a.role = :role")
    Optional<Account> findByUsernameAndRole(@Param("username") String username, @Param("role") Account.RoleType role);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.role = :role")
    long countByRole(@Param("role") Account.RoleType role);
}


