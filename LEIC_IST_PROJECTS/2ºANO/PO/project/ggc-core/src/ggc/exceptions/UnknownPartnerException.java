package ggc.exceptions;

public class UnknownPartnerException extends Exception {

    private static final long serialVersionUID = 202109091821L;

    private String _key;

    public UnknownPartnerException(String key){
        _key = key;
    }

    /** @return key */
    public String getKey(){
        return _key;
    }
}