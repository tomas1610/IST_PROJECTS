package ggc.exceptions;

public class UnknownTransactionException extends Exception {

    private static final long serialVersionUID = 202109091821L;

    private int _key;

    public UnknownTransactionException(int key){
        _key = key;
    }

    /** @return key */
    public int getKey(){
        return _key;
    }
}
