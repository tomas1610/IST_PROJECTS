package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.lang.Math;
import java.util.ArrayList;
import java.io.Serializable;

public class Partner implements Serializable {

    private static final long serialVersionUID = 202108111557L;

    private String _id;
    private String _name;
    private String _adress;
    private Double _points;
    private Double _purchases = 0.00;
    private Double _salesBase = 0.00;
    private Double _salesReal = 0.00;
    private int _transactionsN = 0;
    private PartnerState partnerState;
    private ArrayList<String> _notifications = new ArrayList<String>();

    public Partner(String id,String name,String adress, Double points){
        _id = id;
        _name = name;
        _adress = adress;
        _points = points;
        partnerState = new Normal();
    }

    public Double getPoints(){
        return _points;
    }

    public void setPoints(Double points){
        _points = points;
    }

    public PartnerState getStatus(){
        return partnerState;
    }

    public void setStatus(PartnerState p){
        partnerState = p;
    }

    public int getNTransactions(){
        return _transactionsN;
    }

    public void setNTransactions(int n){
        _transactionsN = n;
    }

    public Double calculateAverage(){
        return (_purchases + _salesReal) / _transactionsN;
    }

    public void updateStatus(){
        if (_points > 2000)
            setStatus(new Selection());
        else if (_points > 25000)
            setStatus(new Elite()); 
    }

    public Double calculatePrice(Double baseValue, int limitDate, int payDate, int n){
        Double price = getStatus().calculatePrice(baseValue, payDate, limitDate, n);
        updatePoints(baseValue,limitDate - payDate);
        updateStatus();
        return price;
    }

    public void updatePoints(Double baseValue, int paymentDelay){
        _points = partnerState.calculatePoints(getPoints(), baseValue, paymentDelay);
    }

    public String getId(){
        return _id;
    }

    public void setId(String id){
        _id = id;
    }

    public String getName(){
        return _name;
    }

    public String getAdress(){
        return _adress;
    }

    public Double getPurchases(){
        return _purchases;
    }

    public void setPurchases(Double purchases){
        _purchases = purchases;
    }

    public Double getSalesBase(){
        return _salesBase;
    }

    public void setSalesBase(Double salesBase){
        _salesBase = salesBase;
    }

    public Double getSalesReal(){
        return _salesReal;
    }

    public void setSalesReal(Double salesReal){
        _salesReal = salesReal;
    }

    public ArrayList<String> getNotifications(){
        return _notifications;
    }

    public void clearNotifications(){
        _notifications = new ArrayList<String>();
    }

    public void addNotification(String notification){
        _notifications.add(notification);
    }

    @Override
    public String toString(){
        return getId() + "|" + getName() + "|" + getAdress() + "|" + partnerState.toString() 
        + "|" + Math.round(getPoints()) + "|" + Math.round(getPurchases()) + "|" +
        Math.round(getSalesBase()) + "|" + Math.round(getSalesReal()) + "|" + Math.round(calculateAverage());
    }
}
