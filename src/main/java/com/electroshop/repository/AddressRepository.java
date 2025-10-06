package com.electroshop.repository;

import com.electroshop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByCustomer_Id(Long customerId);
    
    @Query("SELECT a FROM Address a WHERE a.customer.id = :customerId AND a.isDefault = true")
    Optional<Address> findDefaultByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT a FROM Address a WHERE a.city = :city")
    List<Address> findByCity(@Param("city") String city);
    
    @Query("SELECT a FROM Address a WHERE a.commune = :commune")
    List<Address> findByCommune(@Param("commune") String commune);
}


