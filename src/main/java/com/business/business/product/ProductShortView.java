package com.business.business.product;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.business.business.category.Category;
import com.business.business.tag.Tag;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@EntityView(Product.class)
public interface ProductShortView {
    @IdMapping
    UUID getId();
    String getName();
    String getImageUrl();
    Category getCategory();
    Set<Tag> getTags();
    int getNumberAvailable();
    Double getCostPrice();
    Double getSellingPrice();
    String  getDescription();
    LocalDateTime getUpdatedAt();
}
