package com.business.business;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@MappedSuperclass
@SQLDelete(sql = "UPDATE #{#entityName} SET deleted = true WHERE id = ?")
@FilterDef(
        name = "deletedFilter",
        parameters = @ParamDef(name = "isDeleted", type = Boolean.class)
)
@Filter(name = "deletedFilter", condition = "deleted = :isDeleted")
public abstract class SoftDeletableEntity {

    @Column(nullable = false)
    private boolean deleted = false;

    /*
    * F
    * Inject:
    * private  final EntityManager entityManager;
    public List<Object> toAlsoGetDeleted(LocalDateTime from, LocalDateTime to, String productId) {
        var session = entityManager.unwrap(Session.class);
        session.disableFilter("deletedFilter");
        //.. do whatever like List<Product> allProducts = session.createQuery("from Product", Product.class).getResultList();
    }*/
}