/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
@Table(indexes = {
    @Index(columnList = "category_id", unique = false)
})
public class ItemSubCategory extends DAO {

    //@OneToMany(mappedBy = "subCategory")
    //private List<FurnitureItem> furnitureItems;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    private Long id;
    private String nameEn;
    private String nameFr;
    private String nameDe;
    private String nameRu;
    private Integer ttcSubcategory;
    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory category;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getNameDe() {
        return nameDe;
    }

    public void setNameDe(String nameDe) {
        this.nameDe = nameDe;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public Integer getTtcSubcategory() {
        return ttcSubcategory;
    }

    public void setTtcSubcategory(Integer ttcSubcategory) {
        this.ttcSubcategory = ttcSubcategory;
    }

    @Override
    public String toString() {
        return nameEn;
    }

}
