package com.business.business.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public class ProductDto {
    @NotNull
    public UUID categoryId;

    @NotNull
    public String name;

    public String imageUrl;

    @Size(max = 5, message = "Maximum of 5 tags allowed")
    public Set<String> tags;

    @Min(0)
    public int numberAvailable;

//    @DecimalMin(value = "0.0", inclusive = false)
    public Double costPrice;

    @DecimalMin(value = "0.0", inclusive = false)
    @NotNull
    public Double sellingPrice;

    public String description;
}
