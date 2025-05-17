package com.business.business.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryDto(@NotBlank String categoryName) {
}
