/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
@Table(indexes = {
    @Index(columnList = "parent_id", unique = false)
})
public class FurnitureCategory extends DAO {

    @Id
    @Basic(optional = false)
    private Long id;
    private String textEn;
    private String textDe;
    private String textFr;
    private String textRu;
    @ManyToOne
    private FurnitureCategory parent;
    @OneToMany(mappedBy = "parent")
    private List<FurnitureCategory> childs;
    private Boolean active;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTextEn() {
        return textEn;
    }

    public void setTextEn(String textEn) {
        this.textEn = textEn;
    }

    public FurnitureCategory getParent() {
        return parent;
    }

    public void setParent(FurnitureCategory parent) {
        this.parent = parent;
    }

    public List<FurnitureCategory> getChilds() {
        return childs;
    }

    public void setChilds(List<FurnitureCategory> childs) {
        this.childs = childs;
    }

    public String getTextDe() {
        return textDe;
    }

    public void setTextDe(String textDe) {
        this.textDe = textDe;
    }

    public String getTextFr() {
        return textFr;
    }

    public void setTextFr(String textFr) {
        this.textFr = textFr;
    }

    public String getTextRu() {
        return textRu;
    }

    public void setTextRu(String textRu) {
        this.textRu = textRu;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}
