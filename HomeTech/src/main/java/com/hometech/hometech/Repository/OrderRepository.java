package com.hometech.hometech.Repository;

import com.hometech.hometech.enums.OrderStatus;
import com.hometech.hometech.model.Customer;
import com.hometech.hometech.model.Order;
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
}
