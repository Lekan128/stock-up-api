package com.business.business.sale;


import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.business.business.auth.AuthService;
import com.business.business.exception.BadRequestException;
import com.business.business.product.Product;
import com.business.business.product.ProductRepository;
import com.business.business.store.Store;
import com.business.business.user.Role;
import com.business.business.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.lekan128.aiagent.api.ObjectMapperSingleton;
import io.github.lekan128.aiagent.api.annotation.AiToolMethod;
import io.github.lekan128.aiagent.api.annotation.ArgDesc;
import io.github.lekan128.aiagent.impl.method.caller.ReflectionCaller;
import io.github.lekan128.aiagent.impl.method.caller.ReflectionInvocableMethod;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CriteriaBuilderFactory cbf;
    private final EntityManager em;
    private final EntityViewManager evm;


    @Transactional
    public Sale createSale(SaleRequest request) {
        Store currentUserStore = AuthService.getCurrentAuthenticatedUserStore();
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
                .costPrice(product.getCostPrice())
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

            product.setNumberAvailable(product.getNumberAvailable() - req.getQuantity());

            double totalAmount = product.getSellingPrice() * req.getQuantity();
            return Sale.builder()
                    .product(product)
                    .quantity(req.getQuantity())
                    .soldPrice(product.getSellingPrice())
                    .costPrice(product.getCostPrice())
                    .totalAmount(totalAmount)
                    .build();
        }).collect(Collectors.toList());

        return saleRepository.saveAll(sales);
    }


    public List<Sale> getSales(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = end != null ? end.plusDays(1).atStartOfDay() : null;

        User currentAuthenticatedUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.getRole() == Role.ADMIN){
            return saleRepository.findByDateRange(startDateTime, endDateTime, null);
        }
        if (currentAuthenticatedUser.getStore() == null){
            throw new BadRequestException("Current user does not have a store.");
        }
        UUID storeId = currentAuthenticatedUser.getStore().id;
        return saleRepository.findByDateRange(startDateTime, endDateTime, storeId);
    }


    public List<Sale> getSalesByProduct(UUID productId) {
        return saleRepository.findByProduct(productId);
    }


    public Sale getById(UUID id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));
    }

    @AiToolMethod("Get all the user's sale")
    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public List<Sale> filterSales(LocalDateTime from, LocalDateTime to, String productId) {
        CriteriaBuilder<Sale> cb = cbf.create(em, Sale.class);
        User currentAuthenticatedUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.getRole() != Role.ADMIN && currentAuthenticatedUser.getStore() == null){
            throw new BadRequestException("User does not have a store");
        }
        if (currentAuthenticatedUser.getRole() != Role.ADMIN){
            cb.where("product.store.id").eq(currentAuthenticatedUser.getStore().id);
        }

        if (from != null){
            cb.where("createdAt").gt(from);
        }
        if (to != null){
            cb.where("createdAt").lt(to);
        }

        if (productId != null) cb.where("product.id").eq(productId);
        return cb.getResultList();
    }


    public Page<SaleView> filterSales(LocalDateTime from, LocalDateTime to, String productId,
                                      String sortBy,
                                      String direction, int page, int size) {
        CriteriaBuilder<Sale> cb = cbf.create(em, Sale.class);

        User currentAuthenticatedUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.getRole() != Role.ADMIN && currentAuthenticatedUser.getStore() == null){
            throw new BadRequestException("User does not have a store");
        }
        if (currentAuthenticatedUser.getRole() != Role.ADMIN){
            cb.where("product.store.id").eq(currentAuthenticatedUser.getStore().id);
        }

        if (from != null) {
            cb.where("createdAt").ge(from);
        }
        if (to != null) {
            cb.where("createdAt").le(to);
        }
        if (productId != null) {
            cb.where("product.id").eq(productId);
        }

        if (sortBy != null) {
//            boolean ascending = direction == null || direction.equalsIgnoreCase("asc");
            if (direction == null || direction.equalsIgnoreCase("asc")) {
                cb.orderByAsc(sortBy);
            }
            else if (direction.equalsIgnoreCase("desc")){
                cb.orderByDesc(sortBy);
            }
        } else {
            cb.orderByDesc("createdAt"); // Default order: latest sales first
        }
        cb.orderByAsc("id");

        EntityViewSetting<SaleView, PaginatedCriteriaBuilder<SaleView>> setting =
                EntityViewSetting.create(SaleView.class, page, size);

        PaginatedCriteriaBuilder<SaleView> paginatedCb = evm.applySetting(setting, cb);
        PagedList<SaleView> result = paginatedCb.getResultList();

        ////.....

        // Blaze Persistence pagination
//        PagedList<Sale> resultPage = cb.orderByDesc("createdAt")
//                .page(page, size)
//                .getResultList();

        return new PageImpl<>(
                result,
                PageRequest.of(page, size),
                result.getTotalSize()
        );
    }

    @AiToolMethod("Returns the sales summary the current user")//2025-10-30T00:00:00
    public SalesSummaryView getSalesSummary(@ArgDesc("Start date filter. Local date time format YYYY-MM-DDTHH:MM:SS") @Nullable String from, @ArgDesc("End date filter. Local date time format YYYY-MM-DDTHH:MM:SS") @Nullable String to) {
        LocalDateTime fromDateTime = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDateTime = to != null ? LocalDateTime.parse(to) : null;
        return getSalesSummary(fromDateTime, toDateTime);
    }

    public SalesSummaryView getSalesSummary(LocalDateTime from, LocalDateTime to) {
        User currentAuthenticatedUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.getStore() == null){
            throw new BadRequestException("User does not have a store");
        }

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                .from(Sale.class);

        cb.where("product.store.id").eq(currentAuthenticatedUser.getStore().id);
        if (from != null) cb.where("createdAt").ge(from);
        if (to != null) cb.where("createdAt").le(to);

        // Profit = (soldPrice - costPrice) * quantity
        cb.select("SUM(quantity)", "totalQuantity")
                .select("SUM(totalAmount)", "totalRevenue")
                .select("SUM(CASE WHEN soldPrice IS NULL OR costPrice IS NULL THEN 1 ELSE 0 END)", "excludedCount")
                .select("SUM(CASE WHEN soldPrice IS NOT NULL AND costPrice IS NOT NULL THEN (soldPrice - costPrice) * quantity ELSE 0 END)", "totalProfit");

        Tuple result = cb.getSingleResult();


        Long totalQuantity = result.get("totalQuantity", Long.class);
        Double totalRevenue = result.get("totalRevenue", Double.class);

        return new SalesSummaryView(
                totalQuantity != null ? totalQuantity : 0L,
                totalRevenue != null ? totalRevenue : 0.0,
                Optional.ofNullable(result.get("totalProfit", Double.class)).orElse(0.0),
                Optional.ofNullable(result.get("excludedCount", Long.class)).orElse(0L)
        );
    }


    public List<TopProductView> getTopSellingProducts(LocalDateTime from, LocalDateTime to, int limit) {
        User currentAuthenticatedUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.getStore() == null){
            throw new BadRequestException("User does not have a store");
        }

        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                .from(Sale.class)
                .groupBy("product.id")
                .select("product.id", "productId")
                .select("product.name", "productName")
                .select("SUM(quantity)", "totalSold")
                .select("SUM(totalAmount)", "totalRevenue")
                .orderByAsc("id")
                .orderByDesc("totalSold");

        cb.where("product.store.id").eq(currentAuthenticatedUser.getStore().id);
        if (from != null) cb.where("createdAt").ge(from);
        if (to != null) cb.where("createdAt").le(to);

        List<Tuple> tuples = cb.page(0, limit).getResultList();
        return tuples.stream()
                .map(t -> new TopProductView(
                        t.get("productId", UUID.class),
                        t.get("productName", String.class),
                        t.get("totalSold", Long.class),
                        t.get("totalRevenue", Double.class)
                ))
                .toList();
    }

    public List<ProductStockView> getLowStockProducts(int threshold) {
        User currentAuthenticatedUser = AuthService.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.getStore() == null){
            throw new BadRequestException("User does not have a store");
        }

        CriteriaBuilder<Product> cb = cbf.create(em, Product.class);
        cb.where("product.store.id").eq(currentAuthenticatedUser.getStore().id);
        cb.where("numberAvailable").lt(threshold);
        return evm.applySetting(EntityViewSetting.create(ProductStockView.class), cb).getResultList();
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
