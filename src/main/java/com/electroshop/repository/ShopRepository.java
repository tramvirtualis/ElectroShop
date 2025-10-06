package com.electroshop.repository;

import com.electroshop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    
    List<Shop> findByOwnerUser_Id(Long ownerUserId);
    
    @Query("SELECT s FROM Shop s WHERE s.shopName LIKE %:name%")
    List<Shop> findByShopNameContaining(@Param("name") String name);
    
    @Query("SELECT s FROM Shop s WHERE s.ownerUser.id = :userId")
    Optional<Shop> findByOwnerUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(s) FROM Shop s WHERE s.ownerUser.id = :userId")
    long countByOwnerUserId(@Param("userId") Long userId);
}


