/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scraelos.esofurnituremp.model;

/**
 *
 * @author guest
 */
public class SelectedFurniture {

    private FurnitureItem item;
    private int count = 0;

    public FurnitureItem getItem() {
        return item;
    }

    public void setItem(FurnitureItem item) {
        this.item = item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void add() {
        this.count++;
    }
    
    public void subtract() {
        this.count--;
    }

}
