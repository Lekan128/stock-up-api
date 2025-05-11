package com.business.business.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

//    @Override
//    @Query("select c from Category c where c.id = :id")
//    Optional<Category> findById(UUID id);
}
