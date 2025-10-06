package com.electroshop.repository;

import com.electroshop.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Long> {
    
    Optional<Shipper> findByUser_Id(Long userId);
    
    @Query("SELECT s FROM Shipper s WHERE s.licenseNumber = :licenseNumber")
    Optional<Shipper> findByLicenseNumber(@Param("licenseNumber") String licenseNumber);
    
    @Query("SELECT s FROM Shipper s WHERE s.vehicleType = :vehicleType")
    List<Shipper> findByVehicleType(@Param("vehicleType") String vehicleType);
    
    @Query("SELECT COUNT(s) FROM Shipper s")
    long countAllShippers();
}


