package com.business.business.sale;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TopProductView {
    private UUID productId;
    private String productName;
    private long totalSold;
    private double totalRevenue;
}
