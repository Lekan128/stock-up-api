package com.business.business.sale;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.business.business.product.ProductShortView;

import java.time.LocalDateTime;
import java.util.UUID;

@EntityView(Sale.class)
public interface SaleView {

    @IdMapping
    UUID getId();
    double getSoldPrice();
    int getQuantity();
    double getTotalAmount();
    LocalDateTime getCreatedAt();
    ProductShortView getProduct();
}