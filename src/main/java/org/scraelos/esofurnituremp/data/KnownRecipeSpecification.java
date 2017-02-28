/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.data;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author scraelos
 */
public class KnownRecipeSpecification implements Specification<KnownRecipe> {

    private final SysAccount account;
    private ItemSubCategory category;

    public KnownRecipeSpecification(SysAccount account) {
        this.account = account;
    }

    public void setCategory(ItemSubCategory category) {
        this.category = category;
    }

    @Override
    public Predicate toPredicate(Root<KnownRecipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate result = null;
        Predicate accountPredicate = cb.equal(root.get("account"), account);
        if (category != null) {
            result = cb.and(accountPredicate,
                    cb.equal(root.get("recipe").get("furnitureItem").get("subCategory"), category));
        } else {
            result = accountPredicate;
        }
        return result;
    }

}
