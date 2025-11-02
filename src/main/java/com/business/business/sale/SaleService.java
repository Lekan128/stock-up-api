package com.business.business.sale;


import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.business.business.auth.AuthService;
import com.business.business.exception.BadRequestException;
import com.business.business.product.Product;
import com.business.business.product.ProductRepository;
import com.business.business.store.Store;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CriteriaBuilderFactory cbf;
    private final EntityManager em;


    @Transactional
    public Sale createSale(SaleRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!canCreateOrEditSale(product)){
            throw new BadRequestException("You are not allowed to create or edit this product");
        }

        int quantity = request.getQuantity();
//        if (product.getNumberAvailable() < quantity) {
//            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
//        }

        double soldPrice = product.getSellingPrice();
        double totalAmount = soldPrice * quantity;

        product.setNumberAvailable(product.getNumberAvailable() - quantity);
        Sale sale = Sale.builder()
                .product(product)
                .quantity(quantity)
                .soldPrice(soldPrice)
                .totalAmount(totalAmount)
                .build();


        return saleRepository.save(sale);
    }

    /*@Transactional
    public List<Sale> createMultipleSales(List<SaleRequest> sales) {
        return sales.stream().map(this::createSale).toList();
    }*/

    @Transactional
    public List<Sale> createMultipleSales(List<SaleRequest> requests) {
        List<Sale> sales = requests.stream().map(req -> {
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            if (!canCreateOrEditSale(product)){
                throw new BadRequestException("You are not allowed to create or edit this product");
            }

            double totalAmount = product.getSellingPrice() * req.getQuantity();
            return Sale.builder()
                    .product(product)
                    .quantity(req.getQuantity())
                    .soldPrice(product.getSellingPrice())
                    .totalAmount(totalAmount)
                    .build();
        }).collect(Collectors.toList());

        return saleRepository.saveAll(sales);
    }


    public List<Sale> getSales(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = end != null ? end.plusDays(1).atStartOfDay() : null;
        return saleRepository.findByDateRange(startDateTime, endDateTime);
    }


    public List<Sale> getSalesByProduct(UUID productId) {
        return saleRepository.findByProduct(productId);
    }


    public Sale getById(UUID id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public List<Sale> filterSales(LocalDateTime from, LocalDateTime to, String productId) {
        CriteriaBuilder<Sale> cb = cbf.create(em, Sale.class);
        if (from != null){
            cb.where("createdAt").gt(from);
        }
        if (to != null){
            cb.where("createdAt").lt(to);
        }
//        cb.where("createdAt").between(from).and(to);
        if (productId != null) cb.where("product.id").eq(productId);
        return cb.getResultList();
    }


    public Sale updateSale(UUID id, SaleRequest request) {
        Sale existingSale = getById(id);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!canCreateOrEditSale(product)){
            throw new BadRequestException("You are not allowed to create or edit this product");
        }

        existingSale.setProduct(product);
        existingSale.setQuantity(request.getQuantity());
        existingSale.setSoldPrice(product.getSellingPrice());


        return saleRepository.save(existingSale);
    }


    public void deleteSale(UUID id) {
        saleRepository.deleteById(id);
    }
    private boolean canCreateOrEditSale(Product product){
        Store store = AuthService.getCurrentAuthenticatedUserStore();
        return product.store.id.equals(store.id);
    }
}
