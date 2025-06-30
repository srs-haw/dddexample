package de.haw.swa.ordermanagement.domain.model.order;

import de.haw.swa.ordermanagement.domain.model.shared.Money;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    protected OrderItem() {}
    
    public OrderItem(Long productId, String productName, Money unitPrice, int quantity) {
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        this.productName = Objects.requireNonNull(productName, "Product name cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null").getAmount();
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
        this.totalPrice = unitPrice.multiply(quantity).getAmount();
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public Money getUnitPrice() {
        return Money.euro(unitPrice);
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public Money getTotalPrice() {
        return Money.euro(totalPrice);
    }
    
    // JPA relationship methods
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity.equals(orderItem.quantity) &&
               Objects.equals(productId, orderItem.productId) &&
               Objects.equals(productName, orderItem.productName) &&
               Objects.equals(unitPrice, orderItem.unitPrice);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, unitPrice, quantity);
    }
}