package com.hometech.hometech.Repository;

import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Lấy tất cả đơn hàng của một customer
    List<Order> findByCustomer(Customer customer);
    
    // Lấy đơn hàng của customer theo trạng thái
    List<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus orderStatus);
    
    // Lấy đơn hàng theo trạng thái (cho admin)
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT o FROM Order o WHERE LOWER(o.customer.fullName) LIKE %:keyword% OR LOWER(o.customer.email) LIKE %:keyword%")
    List<Order> searchOrders(@Param("keyword") String keyword);
    
    // Lấy tất cả đơn hàng kèm customer với pagination
    // Note: Using JOIN instead of LEFT JOIN FETCH for pagination compatibility
    // Sort parameter in Pageable will override the default ORDER BY clause
    @Query(value = "SELECT o FROM Order o LEFT JOIN o.customer",
           countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllWithCustomer(Pageable pageable);
    
    // Lấy đơn hàng của customer với pagination
    @Query(value = "SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.orderDate DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.customer = :customer")
    Page<Order> findByCustomerOrderByOrderDateDesc(@Param("customer") Customer customer, Pageable pageable);
    
    // Lấy đơn hàng của customer theo trạng thái với pagination
    @Query(value = "SELECT o FROM Order o WHERE o.customer = :customer AND o.orderStatus = :status ORDER BY o.orderDate DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.customer = :customer AND o.orderStatus = :status")
    Page<Order> findByCustomerAndOrderStatusOrderByOrderDateDesc(@Param("customer") Customer customer, @Param("status") OrderStatus status, Pageable pageable);
    
    // Lấy đơn hàng của customer với trạng thái không bao gồm COMPLETED và CANCELLED, có pagination
    @Query(value = "SELECT o FROM Order o WHERE o.customer = :customer AND o.orderStatus NOT IN :excludedStatuses ORDER BY o.orderDate DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.customer = :customer AND o.orderStatus NOT IN :excludedStatuses")
    Page<Order> findByCustomerAndOrderStatusNotInOrderByOrderDateDesc(@Param("customer") Customer customer, @Param("excludedStatuses") List<OrderStatus> excludedStatuses, Pageable pageable);
}
