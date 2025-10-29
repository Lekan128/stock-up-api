package com.business.business.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductInfo(UUID productId, String productDescription, String imageUrl) {
}
