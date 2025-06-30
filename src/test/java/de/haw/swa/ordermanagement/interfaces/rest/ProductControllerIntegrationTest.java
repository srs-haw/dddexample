package de.haw.swa.ordermanagement.interfaces.rest;

import de.haw.swa.ordermanagement.interfaces.rest.dto.CreateProductRequest;
import io.restassured.RestAssured;

import java.math.BigDecimal;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }
    
    @Test
    void shouldCreateProductSuccessfully() {
        CreateProductRequest createProductRequest = new CreateProductRequest(
            "Test Product",
            "Test Description", 
            new BigDecimal("29.99"),
            10
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(createProductRequest)
        .when()
            .post("/api/products")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo("Test Product"))
            .body("description", equalTo("Test Description"))
            .body("price", equalTo(29.99f))
            .body("currency", equalTo("EUR"))
            .body("stockQuantity", equalTo(10))
            .body("productId", notNullValue());
    }
    
    @Test
    void shouldReturnBadRequestForInvalidProductData() {
        CreateProductRequest createProductRequest = new CreateProductRequest(
            "", // Empty name should trigger validation error
            null,
            new BigDecimal("-10.00"), // Negative price should trigger validation error
            -5 // Negative stock should trigger validation error
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(createProductRequest)
        .when()
            .post("/api/products")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
    
    @Test
    void shouldGetAllProducts() {
        // Create a product first
        CreateProductRequest createProductRequest = new CreateProductRequest(
            "Test Product",
            "Test Description",
            new BigDecimal("19.99"),
            5
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(createProductRequest)
        .when()
            .post("/api/products")
        .then()
            .statusCode(HttpStatus.CREATED.value());
        
        // Get all products
        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("", hasSize(greaterThan(0)))
            .body("[0].productId", notNullValue())
            .body("[0].name", notNullValue())
            .body("[0].price", notNullValue());
    }
    
    @Test
    void shouldGetProductById() {
        // Create a product first
        CreateProductRequest createProductRequest = new CreateProductRequest(
            "Test Product",
            "Test Description",
            new BigDecimal("39.99"),
            15
        );
        
        String productId = given()
            .contentType(ContentType.JSON)
            .body(createProductRequest)
        .when()
            .post("/api/products")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("productId");
        
        // Get the product by ID
        given()
        .when()
            .get("/api/products/{productId}", productId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("productId", equalTo(productId))
            .body("name", equalTo("Test Product"))
            .body("description", equalTo("Test Description"))
            .body("price", equalTo(39.99f))
            .body("stockQuantity", equalTo(15));
    }
    
    @Test
    void shouldReturnNotFoundForNonExistentProduct() {
        String nonExistentProductId = "99999";
        
        given()
        .when()
            .get("/api/products/{productId}", nonExistentProductId)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    void shouldSearchProductsByName() {
        // Create products with different names
        CreateProductRequest product1 = new CreateProductRequest(
            "Laptop Computer",
            "High-performance laptop",
            new BigDecimal("999.99"),
            5
        );
        
        CreateProductRequest product2 = new CreateProductRequest(
            "Desktop Computer",
            "Powerful desktop",
            new BigDecimal("1299.99"),
            3
        );
        
        CreateProductRequest product3 = new CreateProductRequest(
            "Smartphone",
            "Latest smartphone",
            new BigDecimal("599.99"),
            20
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(product1)
        .when()
            .post("/api/products");
        
        given()
            .contentType(ContentType.JSON)
            .body(product2)
        .when()
            .post("/api/products");
        
        given()
            .contentType(ContentType.JSON)
            .body(product3)
        .when()
            .post("/api/products");
        
        // Search for products containing "Computer"
        given()
            .queryParam("name", "Computer")
        .when()
            .get("/api/products/search")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("", hasSize(2))
            .body("name", everyItem(containsStringIgnoringCase("Computer")));
    }
    
    @Test
    void shouldDeleteProduct() {
        // Create a product first
        CreateProductRequest createProductRequest = new CreateProductRequest(
            "Product to Delete",
            "This will be deleted",
            new BigDecimal("9.99"),
            1
        );
        
        String productId = given()
            .contentType(ContentType.JSON)
            .body(createProductRequest)
        .when()
            .post("/api/products")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .path("productId");
        
        // Delete the product
        given()
        .when()
            .delete("/api/products/{productId}", productId)
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
        
        // Verify the product is deleted
        given()
        .when()
            .get("/api/products/{productId}", productId)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentProduct() {
        String nonExistentProductId = "99999";
        
        given()
        .when()
            .delete("/api/products/{productId}", nonExistentProductId)
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}