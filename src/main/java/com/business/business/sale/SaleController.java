package com.business.business.sale;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @PostMapping
    public Sale create(@RequestBody SaleRequest request) {
        return saleService.createSale(request);
    }


    @PostMapping("/bulk")
    public List<Sale> createBulk(@RequestBody List<SaleRequest> requests) {
        return saleService.createMultipleSales(requests);
    }


    @GetMapping("/filter")
    public List<Sale> filterByDate(@RequestParam(required = false) LocalDateTime from, @RequestParam(required = false) LocalDateTime to, @RequestParam(required = false) String productId) {
        return saleService.filterSales(from, to, productId);
    }

    @GetMapping
    public List<Sale> getAll() {
        return saleService.findAll();
    }


    @GetMapping("/product/{productId}")
    public List<Sale> getByProduct(@PathVariable UUID productId) {
        return saleService.getSalesByProduct(productId);
    }


    @GetMapping("/{id}")
    public Sale getById(@PathVariable UUID id) {
        return saleService.getById(id);
    }


    @PutMapping("/{id}")
    public Sale update(@PathVariable UUID id, @RequestBody SaleRequest request) {
        return saleService.updateSale(id, request);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        saleService.deleteSale(id);
    }
}