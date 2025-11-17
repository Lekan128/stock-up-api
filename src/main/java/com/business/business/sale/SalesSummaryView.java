package com.business.business.sale;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SalesSummaryView {
    private long totalQuantity;
    private double totalRevenue;
    private double totalProfit;
    private long excludedCount; // number of sales where costPrice or soldPrice is null

}
