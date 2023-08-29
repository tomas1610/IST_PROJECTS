package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.lang.Math;

public class Purchase extends Transaction{

    private int _payedDate;

    public Purchase(int id,String partnerId,String productId,int amount,Double value, int payDate){
        super(id,partnerId,productId,amount,value);
        _payedDate = payDate;
    }

    public int getPayDate(){
        return _payedDate;
    }

    public String toString(){
        return "COMPRA|" + super.toString() + "|" + getPayDate();
    }
}