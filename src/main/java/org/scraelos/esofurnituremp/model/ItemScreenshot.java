/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.scraelos.esofurnituremp.model.lib.DAO;

/**
 *
 * @author scraelos
 */
@Entity
public class ItemScreenshot extends DAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    private Long id;
    private String fileName;
    //private byte[] screenshot;
    private byte[] thumbnail;
    @ManyToOne
    private FurnitureItem furnitureItem;
    @ManyToOne
    private SysAccount author;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ItemScreenshotFull full;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /*public byte[] getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(byte[] screenshot) {
        this.screenshot = screenshot;
    }*/

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public FurnitureItem getFurnitureItem() {
        return furnitureItem;
    }

    public void setFurnitureItem(FurnitureItem furnitureItem) {
        this.furnitureItem = furnitureItem;
    }

    public SysAccount getAuthor() {
        return author;
    }

    public void setAuthor(SysAccount author) {
        this.author = author;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ItemScreenshotFull getFull() {
        return full;
    }

    public void setFull(ItemScreenshotFull full) {
        this.full = full;
    }

}
