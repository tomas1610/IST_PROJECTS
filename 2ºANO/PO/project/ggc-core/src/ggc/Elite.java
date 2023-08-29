package ggc;

import java.io.Serializable;

public class Elite implements PartnerState, Serializable{

    public Double calculatePoints(Double points, Double baseValue, int paymentDelay){
        if (paymentDelay < -15)
            points = points * 0.75;
        if (paymentDelay >= 0)
            points = points + 10 *baseValue;
        return points;
    }

    public Double calculatePrice(Double baseValue, int payDate, int limitDate, int n){
        if (limitDate - payDate >= n){  //P1
            return baseValue * 0.9;
        }
        else if (limitDate -payDate >= 0 && limitDate - payDate < n){   //P2
            return baseValue * 0.9;
        }
        else if (payDate - limitDate > 0 && payDate - limitDate <= n){  //P3
            return baseValue * 0.95;
        }
        else {          //P4
            return baseValue;
        }
    }

    public String toString(){
        return "ELITE";
    } 
}