package de.haw.swa.ordermanagement.interfaces.rest;

import de.haw.swa.ordermanagement.domain.model.product.Product;
import de.haw.swa.ordermanagement.domain.model.shared.Money;
import de.haw.swa.ordermanagement.domain.repository.ProductRepository;
import de.haw.swa.ordermanagement.interfaces.rest.dto.CreateProductRequest;
import de.haw.swa.ordermanagement.interfaces.rest.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Products", description = "Product catalog management operations")
public class ProductController {
    
    private final ProductRepository productRepository;
    
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @PostMapping
    @Operation(
        summary = "Create a new product",
        description = "Creates a new product in the catalog with the provided details. The product will be assigned a unique ID and added to the inventory."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class),
                examples = @ExampleObject(
                    name = "laptop_example",
                    summary = "Laptop product example",
                    value = """
                        {
                          "productId": "1",
                          "name": "Gaming Laptop",
                          "description": "High-performance gaming laptop with RTX graphics",
                          "price": 1299.99,
                          "currency": "EUR",
                          "stockQuantity": 50
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data provided",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Product creation request",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateProductRequest.class),
                    examples = @ExampleObject(
                        name = "create_laptop",
                        summary = "Create laptop example",
                        value = """
                            {
                              "name": "Gaming Laptop",
                              "description": "High-performance gaming laptop with RTX graphics",
                              "price": 1299.99,
                              "stockQuantity": 50
                            }
                            """
                    )
                )
            )
            CreateProductRequest request) {
        Product product = new Product(
            request.name(),
            request.description(),
            Money.euro(request.price()),
            request.stockQuantity()
        );
        
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ProductResponse.from(savedProduct));
    }
    
    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Retrieves all products from the catalog with their current stock information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "array", implementation = ProductResponse.class),
                examples = @ExampleObject(
                    name = "products_list",
                    summary = "List of products",
                    value = """
                        [
                          {
                            "productId": "1",
                            "name": "Gaming Laptop",
                            "description": "High-performance gaming laptop",
                            "price": 1299.99,
                            "currency": "EUR",
                            "stockQuantity": 50
                          },
                          {
                            "productId": "2",
                            "name": "Smartphone",
                            "description": "Latest model smartphone",
                            "price": 799.99,
                            "currency": "EUR",
                            "stockQuantity": 30
                          }
                        ]
                        """
                )
            )
        )
    })
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> response = products.stream()
            .map(ProductResponse::from)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves a specific product by its unique identifier."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product found and retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Unique identifier of the product", example = "1", required = true)
            @PathVariable Long productId) {
        return productRepository.findById(productId)
            .map(product -> ResponseEntity.ok(ProductResponse.from(product)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Search products by name",
        description = "Searches for products whose names contain the specified search term (case-insensitive)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully (may return empty list)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "array", implementation = ProductResponse.class)
            )
        )
    })
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @Parameter(description = "Search term to match against product names", example = "laptop", required = true)
            @RequestParam String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        List<ProductResponse> response = products.stream()
            .map(ProductResponse::from)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{productId}")
    @Operation(
        summary = "Delete a product",
        description = "Removes a product from the catalog. This operation cannot be undone."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully",
            content = @Content()
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content()
        )
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Unique identifier of the product to delete", example = "1", required = true)
            @PathVariable Long productId) {
        if (productRepository.findById(productId).isPresent()) {
            productRepository.deleteById(productId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}