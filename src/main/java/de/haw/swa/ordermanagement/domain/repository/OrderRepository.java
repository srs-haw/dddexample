package de.haw.swa.ordermanagement.domain.repository;

import de.haw.swa.ordermanagement.domain.model.order.Order;
import de.haw.swa.ordermanagement.domain.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId")
    List<Order> findByCustomerIdQuery(@Param("customerId") Long customerId);
    
}