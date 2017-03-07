/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.data;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.KnownRecipe;
import org.scraelos.esofurnituremp.model.RECIPE_TYPE;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author scraelos
 */
public class KnownRecipeSpecification implements Specification<KnownRecipe> {

    private final SysAccount account;
    private ItemSubCategory category;
    private RECIPE_TYPE recipeType;

    public KnownRecipeSpecification(SysAccount account) {
        this.account = account;
    }

    public void setCategory(ItemSubCategory category) {
        this.category = category;
    }

    public void setRecipeType(RECIPE_TYPE recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    public Predicate toPredicate(Root<KnownRecipe> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate result = null;
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("account"), account));
        if (category != null) {
            predicates.add(cb.equal(root.get("recipe").get("furnitureItem").get("subCategory"), category));
        }
        if (recipeType != null) {
            predicates.add(cb.equal(root.get("recipe").get("recipeType"), recipeType));
        }
        if (predicates.size() > 1) {
            result = cb.and(predicates.toArray(new Predicate[predicates.size()]));
        } else {
            result = predicates.get(0);
        }
        return result;
    }

}
