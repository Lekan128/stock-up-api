package com.business.business.sale;

import com.blazebit.persistence.view.EntityView;
import com.business.business.product.Product;

import java.util.UUID;

@EntityView(Product.class)
public interface ProductStockView {
    UUID getId();
    String getName();
    int getNumberAvailable();
}
/*
* Create a page for to see sales, statistics, and generally manage sales and inventory. The entry point of the page should be #file:ProductList.tsx  so add an intuitive place from ProductList to go there. Make sure #file:ProductList.css  it is intuitive and has a good user experience.

The available endpoints:
@GetMapping("sales/filter")
    public Page<SaleView> filterByDate(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction, //or asc
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return saleService.filterSales(from, to, productId, sortBy, direction, page, size);
    }

    @GetMapping("sales/summary")
    public SalesSummaryView getSalesSummary(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        return saleService.getSalesSummary(from, to);
    }

    @GetMapping("sales/top-products")
    public List<TopProductView> getTopSellingProducts(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "5") int limit //no of products to be returned
    ) {
        return saleService.getTopSellingProducts(from, to, limit);
    }

    @GetMapping("sales/low-stock")
    public List<ProductStockView> getLowStockProducts(@RequestParam(defaultValue = "10") int stockThreshold) { //stock treshold is the hightst stock the product that is filtered for should be
        return saleService.getLowStockProducts(stockThreshold);
    }

Remeber that */