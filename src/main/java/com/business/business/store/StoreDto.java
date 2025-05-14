package com.business.business.store;

import jakarta.validation.constraints.NotBlank;

public record StoreDto(@NotBlank String name, String address) {
}
