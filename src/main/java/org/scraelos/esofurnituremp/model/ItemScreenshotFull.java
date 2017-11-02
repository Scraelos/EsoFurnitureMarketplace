/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
public class ItemScreenshotFull extends DAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    private Long id;
    private byte[] screenshot;
    @OneToOne(cascade = CascadeType.ALL,mappedBy = "full")
    private ItemScreenshot thumb;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(byte[] screenshot) {
        this.screenshot = screenshot;
    }

    public ItemScreenshot getThumb() {
        return thumb;
    }

    public void setThumb(ItemScreenshot thumb) {
        this.thumb = thumb;
    }

}
