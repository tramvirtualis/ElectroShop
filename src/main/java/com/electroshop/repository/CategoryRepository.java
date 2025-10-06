package com.electroshop.repository;

import com.electroshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByCategoryName(String categoryName);
    
    boolean existsByCategoryName(String categoryName);
    
    @Query("SELECT c FROM Category c WHERE c.categoryName LIKE %:name%")
    List<Category> findByCategoryNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Category c ORDER BY c.categoryName ASC")
    List<Category> findAllOrderByCategoryName();
    
    @Query("SELECT COUNT(c) FROM Category c")
    long countAllCategories();
}


