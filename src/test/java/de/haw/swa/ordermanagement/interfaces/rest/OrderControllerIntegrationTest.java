package de.haw.swa.ordermanagement.interfaces.rest;

import de.haw.swa.ordermanagement.domain.repository.CustomerRepository;
import de.haw.swa.ordermanagement.domain.repository.ProductRepository;
import de.haw.swa.ordermanagement.interfaces.rest.dto.CreateOrderRequest;
import io.restassured.RestAssured;

import java.util.List;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Long customerId;
    private Long productId;
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        
        // Use IDs from test data - these are auto-generated via data.sql
        customerId = 1L; // Max Mustermann (first customer)
        productId = 1L; // Laptop (first product)
    }
    
    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
            customerId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 2))
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("customerId", equalTo("1"))
            .body("status", equalTo("PENDING"))
            .body("items", hasSize(1))
            .body("items[0].productId", equalTo("1"))
            .body("items[0].quantity", equalTo(2))
            .body("totalAmount", equalTo(2599.98f))
            .body("currency", equalTo("EUR"));
    }
    
    @Test
    void shouldReturnBadRequestForInvalidOrderData() {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
            customerId,
            List.of() // Empty items list should trigger validation error
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
    
    @Test
    void shouldGetOrderById() {
        // First create an order
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
            customerId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );
        
        String orderId = given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("orderId");
        
        // Then get the order
        given()
        .when()
            .get("/api/orders/{orderId}", orderId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("orderId", equalTo(orderId))
            .body("customerId", equalTo("1"))
            .body("status", equalTo("PENDING"));
    }
    
    @Test
    void shouldReturnNotFoundForNonExistentOrder() {
        String nonExistentOrderId = "99999";
        
        given()
        .when()
            .get("/api/orders/{orderId}", nonExistentOrderId)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    void shouldConfirmOrder() {
        // First create an order
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
            customerId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );
        
        String orderId = given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("orderId");
        
        // Then confirm the order
        given()
        .when()
            .put("/api/orders/{orderId}/confirm", orderId)
        .then()
            .statusCode(HttpStatus.OK.value());
        
        // Verify the order status changed
        given()
        .when()
            .get("/api/orders/{orderId}", orderId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("status", equalTo("CONFIRMED"));
    }
    
    @Test
    void shouldProcessPaymentForConfirmedOrder() {
        // Create and confirm order first
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
            customerId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );
        
        String orderId = given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("orderId");
        
        given()
        .when()
            .put("/api/orders/{orderId}/confirm", orderId)
        .then()
            .statusCode(HttpStatus.OK.value());
        
        // Process payment
        given()
        .when()
            .put("/api/orders/{orderId}/pay", orderId)
        .then()
            .statusCode(HttpStatus.OK.value());
        
        // Verify the order status changed
        given()
        .when()
            .get("/api/orders/{orderId}", orderId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("status", equalTo("PAID"));
    }
    
    @Test
    void shouldGetOrdersByCustomerId() {
        // Create multiple orders for the customer
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
            customerId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );
        
        // Create first order
        given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.CREATED.value());
        
        // Create second order
        given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.CREATED.value());
        
        // Get orders by customer ID
        given()
            .queryParam("customerId", customerId)
        .when()
            .get("/api/orders")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("", hasSize(greaterThanOrEqualTo(2)))
            .body("customerId", everyItem(equalTo("1")));
    }
    
    @Test
    void shouldCancelOrder() {
        // Create an order
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
            customerId,
            List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );
        
        String orderId = given()
            .contentType(ContentType.JSON)
            .body(createOrderRequest)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("orderId");
        
        // Cancel the order
        given()
        .when()
            .put("/api/orders/{orderId}/cancel", orderId)
        .then()
            .statusCode(HttpStatus.OK.value());
        
        // Verify the order status changed
        given()
        .when()
            .get("/api/orders/{orderId}", orderId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("status", equalTo("CANCELLED"));
    }
}