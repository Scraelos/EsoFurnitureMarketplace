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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.scraelos.esofurnituremp.model.Recipe;
import org.scraelos.esofurnituremp.model.SysAccount;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author scraelos
 */
public class FurnitureItemSpecification implements Specification<FurnitureItem> {

    private String searchString;
    private Boolean searchStringIgnoresAll;
    private Boolean onlyCraftable;
    private Boolean hasCrafters;
    private ItemSubCategory category;
    private ITEM_QUALITY itemQuality;
    private ESO_SERVER esoServer;
    private Boolean unknownRecipes;
    private SysAccount account;
    private String crafterId;

    public void setItemQuality(ITEM_QUALITY itemQuality) {
        this.itemQuality = itemQuality;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Boolean getSearchStringIgnoresAll() {
        return searchStringIgnoresAll;
    }

    public void setSearchStringIgnoresAll(Boolean searchStringIgnoresAll) {
        this.searchStringIgnoresAll = searchStringIgnoresAll;
    }

    public Boolean getOnlyCraftable() {
        return onlyCraftable;
    }

    public void setOnlyCraftable(Boolean onlyCraftable) {
        this.onlyCraftable = onlyCraftable;
    }

    public ItemSubCategory getCategory() {
        return category;
    }

    public void setCategory(ItemSubCategory category) {
        this.category = category;
    }

    public void setHasCrafters(Boolean hasCrafters) {
        this.hasCrafters = hasCrafters;
    }

    public void setEsoServer(ESO_SERVER esoServer) {
        this.esoServer = esoServer;
    }

    public void setUnknownRecipes(Boolean unknownRecipes) {
        this.unknownRecipes = unknownRecipes;
    }

    public void setAccount(SysAccount account) {
        this.account = account;
    }

    public void setCrafterId(String crafterId) {
        this.crafterId = crafterId;
    }

    @Override
    public Predicate toPredicate(Root<FurnitureItem> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        Predicate result = null;
        Predicate textSearch = null;
        cq.distinct(true);
        if (searchString != null && searchString.length() > 2) {
            textSearch = cb.or(
                    cb.like(cb.lower(root.get("nameEn")), "%" + searchString.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("nameDe")), "%" + searchString.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("nameFr")), "%" + searchString.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("nameRu")), "%" + searchString.toLowerCase() + "%")
            );
        }
        if (searchStringIgnoresAll != null && searchStringIgnoresAll && textSearch != null) {
            result = textSearch;
        } else {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                predicates.add(cb.equal(root.get("subCategory"), category));
            }
            if ((onlyCraftable != null && onlyCraftable) || (unknownRecipes != null && unknownRecipes)) {
                predicates.add(cb.isNotNull(root.get("recipe")));
            }
            if (crafterId != null && !crafterId.isEmpty()) {
                Path<Object> knownJoin = root.join("recipe").join("knownRecipes");
                predicates.add(cb.equal(knownJoin.get("esoServer"), esoServer));
                predicates.add(cb.equal(knownJoin.get("account").get("esoId"), crafterId));
            } else if (hasCrafters != null && hasCrafters) {
                predicates.add(cb.equal(root.join("recipe").join("knownRecipes").get("esoServer"), esoServer));
            }
            if (itemQuality != null) {
                predicates.add(cb.equal(root.get("itemQuality"), itemQuality));
            }
            if (unknownRecipes != null && unknownRecipes && account != null) {
                Subquery<Recipe> subquery = cq.subquery(Recipe.class);
                Root fromRecipe = subquery.from(Recipe.class);
                subquery.select(fromRecipe);
                subquery.where(cb.and(
                        cb.equal(fromRecipe.join("knownRecipes").get("account"), account),
                        cb.equal(fromRecipe.get("id"), root.get("recipe").get("id"))
                ));
                predicates.add(cb.not(root.get("recipe").in(subquery)));
            }
            if (textSearch != null) {
                predicates.add(textSearch);
            }
            if (!predicates.isEmpty() && predicates.size() > 1) {
                result = cb.and(predicates.toArray(new Predicate[predicates.size()]));
            } else if (!predicates.isEmpty()) {
                result = predicates.get(0);
            }

        }

        return result;
    }

}
