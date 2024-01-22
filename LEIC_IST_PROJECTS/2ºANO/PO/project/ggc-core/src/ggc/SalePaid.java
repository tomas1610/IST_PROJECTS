package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.lang.Math;

public class SalePaid extends Transaction {

    private Double _realValue;
    private int _limitDate;
    private int _payedAt;

    public SalePaid(int id,String partnerId,String productId,int amount,Double baseValue,Double realValue,int limitDate,int payDate){
        super(id,partnerId,productId,amount,baseValue);
        _realValue = realValue;
        _limitDate = limitDate;
        _payedAt = payDate;
    }

    public Double getRealValue(){
        return _realValue;
    }

    public int getLimitDate(){
        return _limitDate;
    }

    public int getPayDate(){
        return _payedAt;
    }

    public String toString(){
        return "VENDA|" + super.toString() + "|" + Math.round(getRealValue()) + "|" + getLimitDate() + "|" + getPayDate();
    }
}