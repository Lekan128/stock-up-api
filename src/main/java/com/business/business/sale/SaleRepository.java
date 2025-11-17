package com.business.business.sale;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    @Query("SELECT s FROM Sale s WHERE (:start IS NULL OR s.createdAt >= :start) AND (:end IS NULL OR s.createdAt <= :end)  AND (:storeId IS NULL OR s.product.store.id = :storeId)")
    List<Sale> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("storeId") UUID storeId);


    @Query("SELECT s FROM Sale s WHERE (:productId IS NULL OR s.product.id = :productId)")
    List<Sale> findByProduct(@Param("productId") UUID productId);
}