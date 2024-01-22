package ggc.exceptions;

public class UnknownProductException extends Exception {

    private static final long serialVersionUID = 202109091821L;

    private String _key;

    public UnknownProductException(String key){
        _key = key;
    }

    /** @return key */
    public String getKey(){
        return _key;
    }
}