package com.electroshop.repository;

import com.electroshop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    Optional<Cart> findByCustomer_Id(Long customerId);
    
    @Query("SELECT c FROM Cart c WHERE c.customer.user.id = :userId")
    Optional<Cart> findByCustomerUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.customer.user.email = :email")
    Optional<Cart> findByCustomerUserEmail(@Param("email") String email);
    
    boolean existsByCustomer_Id(Long customerId);
}


