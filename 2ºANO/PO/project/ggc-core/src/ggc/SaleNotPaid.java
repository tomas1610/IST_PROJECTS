package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;

public class SaleNotPaid extends Transaction {

    private Double _realValue;
    private int _limitDate;

    public SaleNotPaid(int id,String partnerId,String productId,int amount,Double baseValue,Double realValue,int limitDate){
        super(id,partnerId,productId,amount,baseValue);
        _realValue = realValue;
        _limitDate = limitDate;
    }

    public Double getRealValue(){
        return _realValue;
    }

    public int getLimitDate(){
        return _limitDate;
    }

    public String toString(){
        return "VENDA" + "|" + super.toString() + "|" + Math.round(getRealValue()) + "|" + getLimitDate();
    }
}