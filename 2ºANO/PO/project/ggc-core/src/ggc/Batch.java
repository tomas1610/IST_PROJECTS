package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.io.Serializable;
import java.lang.Math;

public class Batch implements Serializable {

    private static final long serialVersionUID = 202108111557L;

    private int _id;
    private String _partnerId;
    private int _stock;
    private String _productId;
    private Double _price;

    public Batch(int id, String partnerId, String productId, Double price, int stock){
        _id = id;
        _partnerId = partnerId;
        _stock = stock;
        _productId = productId;
        _price = price;
    }

    public int getId(){
        return _id;
    }

    public String getPartnerId(){
        return _partnerId;
    }

    public int getStock(){
        return _stock;
    }

    public void setStock(int stock){
        _stock = stock;
    }

    public Double getPrice(){
        return _price;
    }

    public String getProductId(){
        return _productId;
    }

    public String toString(){
        return getProductId() + "|" + getPartnerId() + "|" + Math.round(getPrice()) + "|" + getStock();
    }
}           
