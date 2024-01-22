package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.io.Serializable;
import java.lang.Math;

public class Transaction implements Serializable{

    public static final long serialVersionUID = 2021080111708L;

    private int _id;
    private String _partnerId;
    private String _productId;
    private int _amount;
    private Double _baseValue;

    public Transaction(int id, String partnerId, String productId, int amount, Double baseValue){
        _id = id;
        _partnerId = partnerId;
        _productId = productId;
        _amount = amount;
        _baseValue = baseValue;
    }

    public int getId(){
        return _id;
    }

    public String getPartnerId(){
        return _partnerId;
    }

    public String getProductId(){
        return _productId;
    }

    public int getAmount(){
        return _amount;
    }

    public Double getBaseValue(){
        return _baseValue;
    }

    public String toString(){
        return getId() + "|" + getPartnerId() + "|" + getProductId() + "|" + getAmount() + "|" + Math.round(getBaseValue());
    }
}