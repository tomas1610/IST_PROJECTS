package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.lang.Math;

public class Breakdown extends Transaction{

    private Double _realValue;
    private String _recipe;
    private int _payedDate;

    public Breakdown(int id, String partnerKey, String productKey, int amount, Double baseValue, Double realValue, String recipe, int payDate){
        super(id,partnerKey,productKey,amount,baseValue);
        _realValue = realValue;
        _recipe = recipe;
        _payedDate = payDate;
    }

    public Double getRealValue(){
        return _realValue;
    }

    public String getRecipe(){
        return _recipe;
    }

    public int getPayDate(){
        return _payedDate;
    }

    public String toString(){
        return "DESAGREGAÇÃO|" + getId() + "|" + getPartnerId() + "|" + getProductId() + "|" + getAmount() + "|" 
        + Math.round(getBaseValue()) + "|" + Math.round(getRealValue()) + "|" + getPayDate() + "|" + getRecipe();
    }
}