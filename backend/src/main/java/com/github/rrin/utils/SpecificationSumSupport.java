package com.github.rrin.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Base for custom repository fragments that aggregate over the same Specifications used for paging,
 * so a list and its total are always computed from identical filters.
 */
public abstract class SpecificationSumSupport<T> {

    private final EntityManager entityManager;
    private final Class<T> domainClass;

    protected SpecificationSumSupport(EntityManager entityManager, Class<T> domainClass) {
        this.entityManager = entityManager;
        this.domainClass = domainClass;
    }

    /** SELECT SUM(attribute) FROM T WHERE spec — returns ZERO instead of null when nothing matches. */
    protected BigDecimal sum(Specification<T> spec, String attribute) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<T> root = query.from(domainClass);

        query.select(cb.sum(root.<BigDecimal>get(attribute)));

        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        BigDecimal result = entityManager.createQuery(query).getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }
}
