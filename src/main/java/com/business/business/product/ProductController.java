package com.business.business.product;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody @Valid ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @PostMapping("list")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Product> createProducts(@RequestBody @Valid List<ProductDto> productDtos) {
        return productService.createProducts(productDtos);
    }

    @GetMapping("all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping
    public ResponseEntity<List<ProductShortView>> findAll(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") Boolean searchTags
    ){
        List<ProductShortView> productShortViews = productService.filter(categoryId, search, searchTags);
        return ResponseEntity.ok(productShortViews);
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable UUID id, @RequestBody @Valid ProductDto productDto) {
        return productService.updateProduct(id, productDto);
    }

    @PatchMapping("image/{id}")
    public Product updateProductImage(@PathVariable UUID id, @RequestBody @Valid ProductImageDto productImageDto) {
        return productService.updateProductImage(id, productImageDto.imageUrl());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
    }
}
