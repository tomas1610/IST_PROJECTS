package ggc.exceptions;

public class NegativeDateException extends Exception {
    
    private static final long serialVersionUID = 201409301048L;

  /** NegativeDateException specification. */
    private int _numberOfDays;


   /** @param numberOfDays */

  public NegativeDateException(int numberOfDays){
      _numberOfDays = numberOfDays;
  }

  /**
   * @return the negative date exception specification.
   */
  
  public int getDays(){
      return _numberOfDays;
  }

}