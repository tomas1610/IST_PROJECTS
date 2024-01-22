package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.util.ArrayList;
import java.util.Map;
import java.lang.Math;

public class MixedProduct extends Product{

    private Double _alpha;
    private String _recipe;
    private int _n = 3;

    public MixedProduct(String id,Double price, int stock, String partnerId, Double alpha, String recipe, ArrayList<String> notifications,int cheaperBatch){
        super(id,price,stock,partnerId,notifications,cheaperBatch);
        _alpha = alpha;
        _recipe = recipe;
    }

    public String getRecipe(){
        return _recipe;
    }

    public Double getAlpha(){
        return _alpha;
    }

    public int getN(){
        return _n;
    }
    
    public String toString(){
        return super.toString() + "|" + getAlpha() + "|" + getRecipe();
    }
}           
