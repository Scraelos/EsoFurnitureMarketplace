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
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
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
    private String searchString;
    private Boolean searchStringIgnoresAll;
    private ITEM_QUALITY itemQuality;
    private ItemSubCategory category;
    private RECIPE_TYPE recipeType;

    public KnownRecipeSpecification(SysAccount account) {
        this.account = account;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public void setSearchStringIgnoresAll(Boolean searchStringIgnoresAll) {
        this.searchStringIgnoresAll = searchStringIgnoresAll;
    }

    public void setItemQuality(ITEM_QUALITY itemQuality) {
        this.itemQuality = itemQuality;
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
        Predicate textSearch = null;
        if (searchString != null && searchString.length() > 2) {
            textSearch = cb.or(
                    cb.like(cb.lower(root.get("recipe").get("nameEn")), "%" + searchString.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("recipe").get("nameDe")), "%" + searchString.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("recipe").get("nameFr")), "%" + searchString.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("recipe").get("nameRu")), "%" + searchString.toLowerCase() + "%")
            );
        }
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("account"), account));
        if (searchStringIgnoresAll != null && searchStringIgnoresAll && textSearch != null) {
            predicates.add(textSearch);
        } else {
            if (category != null) {
                predicates.add(cb.equal(root.get("recipe").get("furnitureItem").get("subCategory"), category));
            }
            if (recipeType != null) {
                predicates.add(cb.equal(root.get("recipe").get("recipeType"), recipeType));
            }
            if (itemQuality != null) {
                predicates.add(cb.equal(root.get("recipe").get("itemQuality"), itemQuality));
            }
            if (textSearch != null) {
                predicates.add(textSearch);
            }
        }
        if (predicates.size() > 1) {
            result = cb.and(predicates.toArray(new Predicate[predicates.size()]));
        } else {
            result = predicates.get(0);
        }
        return result;
    }

}
