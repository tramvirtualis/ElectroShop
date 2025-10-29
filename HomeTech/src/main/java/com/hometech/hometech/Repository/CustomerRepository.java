package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUser_Id(Long userId);
    List<Customer> findAllByUser_Id(Long userId);
}
