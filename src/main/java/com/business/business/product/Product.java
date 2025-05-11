package com.business.business.product;

import com.business.business.category.Category;
import com.business.business.tag.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column
    public String imageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    public Category category;

//    @ElementCollection
//    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
//    @Column(name = "tag", length = 50)
//    public Set<String> tags; // Max 5 tags, validation to be handled elsewhere

//    Todo: use this later
    @ManyToMany
    @JoinTable(
            name = "product_tag",
            joinColumns = @JoinColumn(name = "tag"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    public Set<Tag> tags;

    @Column(nullable = false, name = "number_available")
    public int numberAvailable;

    public Double costPrice;

    @Column(nullable = false)
    public Double sellingPrice;

    @Column
    public String description;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}
