package com.business.business.product;

import jakarta.validation.constraints.NotBlank;

public record ProductImageDto (@NotBlank String imageUrl) {
}
