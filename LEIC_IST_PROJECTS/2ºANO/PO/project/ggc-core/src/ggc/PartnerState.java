package ggc;

public interface PartnerState{

    public Double calculatePoints(Double points, Double baseValue, int paymentDelay);

    public Double calculatePrice(Double baseValue, int payDate, int limitDate, int n);
}