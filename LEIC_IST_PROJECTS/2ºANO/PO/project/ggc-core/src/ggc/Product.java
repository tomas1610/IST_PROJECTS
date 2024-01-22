package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.util.ArrayList;
import java.util.Map;
import java.lang.Math;
import java.io.Serializable;

public class Product implements Serializable{

    public static final long serialVersionUID = 2021080111612L;

    private String _id;
    private Double _price;
    private int _availableStock;
    private String  _partnerId;
    private ArrayList<String> _notifications;
    private int _cheaperBatch;
    private int _n = 5;
    
    public Product(String id,Double price, int stock, String partnerId, ArrayList<String> notifications, int cheaperBatch){
        _price = price;
        _id = id;
        _availableStock = stock;
        _partnerId = partnerId;
        _notifications = notifications;
        _cheaperBatch = cheaperBatch;
    }

    public ArrayList<String> getPartnersNotified(){
        return _notifications;
    }

    public String getId(){
        return _id;
    }

    public Double getPrice(){
        return _price;
    }

    public void setPrice(Double price){
        _price = price;
    }

    public int getStock(){
        return _availableStock;
    }

    public String getPartnerId(){
        return _partnerId;
    }

    public void setStock(int stock){
        _availableStock = stock;
    }

    public void disablePartnerNoti(String partnerId){
        _notifications.remove(partnerId);
    }

    public void enablePartnerNoti(String partnerId){
        _notifications.add(partnerId);
    }

    public int getCheaperBatch(){
        return _cheaperBatch;
    }

    public void setCheaperBatch(int cheaperBatch){
        _cheaperBatch = cheaperBatch;
    }

    public int getN(){
        return _n;
    }
    
    public String toString(){
        return getId() + "|" + Math.round(getPrice()) + "|" + getStock();
    }
}           
