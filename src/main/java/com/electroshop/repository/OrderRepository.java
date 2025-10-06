package com.electroshop.repository;

import com.electroshop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomer_Id(Long customerId);
    
    List<Order> findByOrderStatus(Order.Status status);
    
    List<Order> findByShipper_Id(Long shipperId);
    
    @Query("SELECT o FROM Order o WHERE o.customer.user.id = :userId")
    List<Order> findByCustomerUserId(@Param("userId") Long userId);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC")
    List<Order> findByCustomerIdOrderByOrderDateDesc(@Param("customerId") Long customerId);
    
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status ORDER BY o.orderDate ASC")
    List<Order> findByStatusOrderByOrderDate(@Param("status") Order.Status status);
    
    @Query("SELECT o FROM Order o WHERE o.shipper.id = :shipperId AND o.orderStatus = :status")
    List<Order> findByShipperAndStatus(@Param("shipperId") Long shipperId, @Param("status") Order.Status status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
    long countByStatus(@Param("status") Order.Status status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status")
    Page<Order> findByStatus(@Param("status") Order.Status status, Pageable pageable);
}


