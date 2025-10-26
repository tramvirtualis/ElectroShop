package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUser_Id(Long userId);
}
