package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.util.ArrayList;
import java.io.Serializable;
import java.lang.Math;

public class Notification implements Serializable{

    public static final long serialVersionUID = 2021080111707L;

    private String _productId;
    private Double _price;
    private String _type;

    public Notification(String productId, Double price, String type){
        _productId = productId;
        _price = price;
        _type = type;
    }

    public String getProductId(){
        return _productId;
    }

    public Double getPrice(){
        return _price;
    }

    public String getType(){
        return _type;
    }

    public String toString(){
        return getType() + "|" + getProductId() + "|" + Math.round(getPrice());
    }
}