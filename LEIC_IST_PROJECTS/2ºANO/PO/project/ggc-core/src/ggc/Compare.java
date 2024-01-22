package ggc;

import ggc.Warehouse;
import ggc.WarehouseManager;
import java.util.Comparator;
import java.lang.Math;

public class Compare implements Comparator<Batch>{

  public int compare(Batch b1, Batch b2){
    if (b1.getProductId().compareToIgnoreCase(b2.getProductId()) == 0){
      if (b1.getPartnerId().compareToIgnoreCase(b2.getPartnerId()) == 0){
        if (b1.getPrice() - b2.getPrice() == 0){
          return (b1.getStock() - b2.getStock());}
        return (int)Math.round(b1.getPrice() - b2.getPrice());}
      return b1.getPartnerId().compareToIgnoreCase(b2.getPartnerId());}
    return b1.getProductId().compareToIgnoreCase(b2.getProductId());
  }

}