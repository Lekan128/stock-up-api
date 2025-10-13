package com.business.business.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductShortDto {

    @NotBlank
    public String name;

    @Min(0)
    @NotNull
    public Integer numberAvailable;

    public Double costPrice;

    @DecimalMin(value = "0.0", inclusive = false)
    @NotNull
    public Double sellingPrice;
}
