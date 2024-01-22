package ggc;

import java.io.Serializable;

public class Selection implements PartnerState, Serializable{

    public Double calculatePoints(Double points, Double baseValue, int paymentDelay){
        if (paymentDelay < -2)
            points = points * 0.9;
        if (paymentDelay >= 0)
            points = points + 10 *baseValue;
        return points;
    }

    public Double calculatePrice(Double baseValue, int payDate, int limitDate, int n){
        if (limitDate - payDate >= n){  //P1
            return baseValue * 0.9;
        }
        else if (limitDate -payDate >= 0 && limitDate - payDate < n){   //P2
            if (limitDate - payDate >= 2)
                return baseValue * 0.95;
            return baseValue;
        }
        else if (payDate - limitDate > 0 && payDate - limitDate <= n){  //P3
            if (payDate - limitDate > 1)
                return baseValue + baseValue * (payDate - limitDate) * 0.02;
            return baseValue;
        }
        else{          //P4
            return baseValue * 0.05 * (payDate - limitDate);
        }
    }

    public String toString(){
        return "SELECTION";
    } 
}