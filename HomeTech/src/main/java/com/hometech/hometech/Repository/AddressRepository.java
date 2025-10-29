package com.hometech.hometech.Repository;

import com.hometech.hometech.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AddressRepository  extends JpaRepository<Address, Integer> {
    Optional<Address> findByCustomer_Id(Long userId);
}
