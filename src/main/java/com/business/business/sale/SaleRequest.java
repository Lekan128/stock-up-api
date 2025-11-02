package com.business.business.sale;

import lombok.Getter;

import java.util.UUID;

@Getter
class SaleRequest {
    private UUID productId;
    private int quantity;
}