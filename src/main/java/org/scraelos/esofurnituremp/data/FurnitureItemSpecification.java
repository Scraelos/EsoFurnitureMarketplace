/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.scraelos.esofurnituremp.model.ESO_SERVER;
import org.scraelos.esofurnituremp.model.FURNITURE_THEME;
import org.scraelos.esofurnituremp.model.FurnitureCategory;
import org.scraelos.esofurnituremp.model.FurnitureItem;
import org.scraelos.esofurnituremp.model.ITEM_QUALITY;
import org.scraelos.esofurnituremp.model.ONLINE_STATUS;
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
    private Set<FurnitureCategory> categories;
    private ITEM_QUALITY itemQuality;
    private FURNITURE_THEME theme;
    private ESO_SERVER esoServer;
    private Boolean unknownRecipes;
    private SysAccount account;
    private String crafterId;
    private Long minId;

    public void setMinId(Long minId) {
        this.minId = minId;
    }

    public void setItemQuality(ITEM_QUALITY itemQuality) {
        this.itemQuality = itemQuality;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public void setSearchStringIgnoresAll(Boolean searchStringIgnoresAll) {
        this.searchStringIgnoresAll = searchStringIgnoresAll;
    }

    public void setOnlyCraftable(Boolean onlyCraftable) {
        this.onlyCraftable = onlyCraftable;
    }

    public void setTheme(FURNITURE_THEME theme) {
        this.theme = theme;
    }

    public void setCategories(Set<FurnitureCategory> list) {
        this.categories = list;
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
            if (minId != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("id"), minId));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(cb.or(root.get("category").in(categories), root.get("category").get("parent").in(categories)));
            }
            if ((onlyCraftable != null && onlyCraftable) || (unknownRecipes != null && unknownRecipes)) {
                predicates.add(cb.isNotNull(root.get("recipe")));
            }
            if (crafterId != null && !crafterId.isEmpty()) {
                Path<Object> knownJoin = root.join("recipe").join("knownRecipes");
                predicates.add(cb.equal(knownJoin.get("esoServer"), esoServer));
                predicates.add(cb.equal(knownJoin.get("account").get("esoId"), crafterId));
            } else if (hasCrafters != null && hasCrafters) {
                Join<Object, Object> knownJoin = root.join("recipe").join("knownRecipes");
                predicates.add(cb.and(
                        cb.equal(knownJoin.get("esoServer"), esoServer),
                        cb.notEqual(knownJoin.get("account").get("onlineStatus"), ONLINE_STATUS.Invisible)
                )
                );
            }
            if (itemQuality != null) {
                predicates.add(cb.equal(root.get("itemQuality"), itemQuality));
            }
            if (theme != null) {
                predicates.add(cb.equal(root.get("theme"), theme));
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
