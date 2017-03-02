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
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.ItemSubCategory;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author scraelos
 */
public class FurnitureItemSpecification implements Specification<FurnitureItem> {

    private String searchString;
    private Boolean searchStringIgnoresAll;
    private Boolean onlyCraftable;
    private ItemSubCategory category;
    private ITEM_QUALITY itemQuality;

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

    @Override
    public Predicate toPredicate(Root<FurnitureItem> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        Predicate result = null;
        Predicate textSearch = null;
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
            if (onlyCraftable != null && onlyCraftable) {
                predicates.add(cb.isNotNull(root.get("recipe")));
            }
            if (itemQuality != null) {
                predicates.add(cb.equal(root.get("itemQuality"), itemQuality));
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
